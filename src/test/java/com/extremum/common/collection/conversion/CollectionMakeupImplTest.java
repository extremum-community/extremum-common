package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.CollectionReference;
import com.extremum.common.collection.EmbeddedCoordinates;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.CommonResponseDto;
import com.extremum.common.stucts.IdOrObjectStruct;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionMakeupImplTest {
    @InjectMocks
    private CollectionMakeupImpl collectionMakeup;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;

    private StreetResponseDto streetDto;
    private final CollectionDescriptor descriptorInDB = CollectionDescriptor.forEmbedded(
            new Descriptor("the-street"), "the-buildings");

    @Before
    public void setUp() {
        BuildingResponseDto building1 = new BuildingResponseDto("building1", "address1");
        BuildingResponseDto building2 = new BuildingResponseDto("building2", "address2");
        List<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildings = Arrays.asList(
                new IdOrObjectStruct<Descriptor, BuildingResponseDto>(building1),
                new IdOrObjectStruct<Descriptor, BuildingResponseDto>(building2)
        );
        streetDto = new StreetResponseDto("the-street", new CollectionReference<>(buildings));

        when(collectionDescriptorService.retrieveByCoordinates(anyString())).thenReturn(Optional.empty());
    }

    @Test
    public void givenNoCollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldBeFilledAndSaved() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = streetDto.buildings.getDescriptor();
        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor);

        verify(collectionDescriptorService).store(descriptor);
    }

    private void assertThatStreetBuildingsCollectionGotMakeupApplied(CollectionDescriptor descriptor) {
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getCoordinates(), is(notNullValue()));
        EmbeddedCoordinates embeddedCoordinates = descriptor.getCoordinates().getEmbeddedCoordinates();
        assertThat(embeddedCoordinates, is(notNullValue()));
        assertThat(embeddedCoordinates.getHostId().getExternalId(), is("the-street"));
        assertThat(embeddedCoordinates.getHostFieldName(), is("the-buildings"));
    }

    @Test
    public void givenACollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldNotBeSaved() {
        when(collectionDescriptorService.retrieveByCoordinates(anyString())).thenReturn(Optional.of(descriptorInDB));

        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = streetDto.buildings.getDescriptor();
        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor);

        verify(collectionDescriptorService, never()).store(any());
    }

    private static class BuildingResponseDto extends CommonResponseDto {
        public String address;

        BuildingResponseDto(String externalId, String address) {
            this.id = new Descriptor(externalId);
            this.address = address;
        }
    }

    private static class StreetResponseDto extends CommonResponseDto {
        @MongoEmbeddedCollection(hostFieldName = "the-buildings")
        public CollectionReference<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildings;

        StreetResponseDto(String externalId,
                CollectionReference<IdOrObjectStruct<Descriptor, BuildingResponseDto>> buildings) {
            this.id = new Descriptor(externalId);
            this.buildings = buildings;
        }
    }
}