package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.CollectionReference;
import com.extremum.common.collection.OwnedCoordinates;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.AbstractResponseDto;
import com.extremum.common.stucts.IdOrObjectStruct;
import com.extremum.common.urls.ApplicationUrls;
import com.extremum.common.urls.TestApplicationUrls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
public class CollectionMakeupImplTest {
    @InjectMocks
    private CollectionMakeupImpl collectionMakeup;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;
    @Spy
    private ApplicationUrls applicationUrls = new TestApplicationUrls();

    private StreetResponseDto streetDto;
    private final CollectionDescriptor descriptorInDB = CollectionDescriptor.forOwned(
            new Descriptor("the-street"), "the-buildings");

    @BeforeEach
    public void setUp() {
        BuildingResponseDto building1 = new BuildingResponseDto("building1", "address1");
        BuildingResponseDto building2 = new BuildingResponseDto("building2", "address2");
        List<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildings = Arrays.asList(
                new IdOrObjectStruct<Descriptor, BuildingResponseDto>(building1),
                new IdOrObjectStruct<Descriptor, BuildingResponseDto>(building2)
        );
        streetDto = new StreetResponseDto("the-street", buildings);
    }

    @Test
    public void givenNoCollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldBeFilledAndSaved() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = streetDto.buildings.getId();
        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-buildings");

        verify(collectionDescriptorService).store(descriptor);
    }

    private void assertThatStreetBuildingsCollectionGotMakeupApplied(CollectionDescriptor descriptor,
            String expectedHostFieldName) {
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getCoordinates(), is(notNullValue()));
        OwnedCoordinates ownedCoordinates = descriptor.getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates, is(notNullValue()));
        assertThat(ownedCoordinates.getHostId().getExternalId(), is("the-street"));
        assertThat(ownedCoordinates.getHostFieldName(), is(expectedHostFieldName));
    }

    @Test
    public void givenACollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldNotBeSaved() {
        when(collectionDescriptorService.retrieveByCoordinates(anyString())).thenReturn(Optional.of(descriptorInDB));

        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = streetDto.buildings.getId();
        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-buildings");

        verify(collectionDescriptorService, never()).store(any());
    }

    @Test
    public void whenApplyingCollectionMakeup_thenPrivateFieldsAreProcessedToo() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = streetDto.privateBuildings.getId();
        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-private-buildings");

        verify(collectionDescriptorService).store(descriptor);
    }

    @Test
    public void givenADtoHasNullId_whenApplyCollectionMakeup_thenShouldNotChangeNothing() {
        streetDto.setId(null);

        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.buildings.getId(), is(nullValue()));
        verify(collectionDescriptorService, never()).store(any());
    }

    @Test
    public void givenADtoHasNullCollectionReference_whenApplyCollectionMakeup_thenShouldNotChangeNothing() {
        streetDto.buildings = null;

        collectionMakeup.applyCollectionMakeup(streetDto);
    }

    @Test
    public void givenHostFieldNameIsNotSpecified_whenApplyingCollectionMakeup_thenHostFieldNameIsDeducedFromFieldName() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = streetDto.buildingsWithDefaultName.getId();
        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "buildingsWithDefaultName");

        verify(collectionDescriptorService).store(descriptor);
    }

    @Test
    public void whenMakeupIsApplied_thenUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        String collectionId = streetDto.buildings.getId().getExternalId();
        assertThat(streetDto.buildings.getUrl(), is("https://example.com/collection/" + collectionId));
    }

    private static class BuildingResponseDto extends AbstractResponseDto {
        public String address;

        BuildingResponseDto(String externalId, String address) {
            setId(new Descriptor(externalId));
            this.address = address;
        }
    }

    private static class StreetResponseDto extends AbstractResponseDto {
        @OwnedCollection(hostFieldName = "the-buildings")
        public CollectionReference<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildings;
        @OwnedCollection(hostFieldName = "the-private-buildings")
        private CollectionReference<IdOrObjectStruct<Descriptor, BuildingResponseDto>> privateBuildings;
        @OwnedCollection
        public CollectionReference<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildingsWithDefaultName;

        StreetResponseDto(String externalId,
                List<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(externalId));
            this.buildings = new CollectionReference<>(buildings);
            this.privateBuildings = new CollectionReference<>(buildings);
            this.buildingsWithDefaultName = new CollectionReference<>(buildings);
        }
    }
}