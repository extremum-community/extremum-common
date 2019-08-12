package io.extremum.common.collection.conversion;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import io.extremum.common.collection.OwnedCoordinates;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.dto.AbstractResponseDto;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.urls.TestApplicationUrls;
import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class CollectionMakeupImplTest {
    @InjectMocks
    private CollectionMakeupImpl collectionMakeup;

    @Spy
    private CollectionDescriptorService collectionDescriptorService = new InMemoryCollectionDescriptorService();
    @Spy
    private ApplicationUrls applicationUrls = new TestApplicationUrls();

    private StreetResponseDto streetDto;
    private final CollectionDescriptor descriptorInDB = CollectionDescriptor.forOwned(
            new Descriptor("the-street"), "the-buildings");
    private OuterResponseDto outerDto;
    private OuterResponseDtoWithObjectTyping outerResponseDtoWithObjectTyping;

    @BeforeEach
    void setUp() {
        BuildingResponseDto building1 = new BuildingResponseDto("building1", "address1");
        BuildingResponseDto building2 = new BuildingResponseDto("building2", "address2");
        List<IdOrObject<Descriptor, BuildingResponseDto>> buildings = Arrays.asList(
                new IdOrObject<Descriptor, BuildingResponseDto>(building1),
                new IdOrObject<Descriptor, BuildingResponseDto>(building2)
        );
        streetDto = new StreetResponseDto("the-street", buildings);

        outerDto = new OuterResponseDto("outer-id", "inner-id", buildings);
        outerResponseDtoWithObjectTyping = new OuterResponseDtoWithObjectTyping("outer-id", "inner-id", buildings);
    }

    @Test
    void givenNoCollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldBeFilledAndSaved() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        String collectionId = streetDto.buildings.getId();
        CollectionDescriptor descriptor = retrieveNonNullCollectionDescriptor(collectionId);

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-buildings");
    }

    @NotNull
    private CollectionDescriptor retrieveNonNullCollectionDescriptor(String collectionId) {
        assertThat(collectionId, is(notNullValue()));

        CollectionDescriptor descriptor = collectionDescriptorService.retrieveByExternalId(collectionId)
                .orElse(null);
        assertThat(descriptor, is(notNullValue()));
        return descriptor;
    }

    private void assertThatStreetBuildingsCollectionGotMakeupApplied(CollectionDescriptor descriptor,
            String expectedHostAttributeName) {
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getCoordinates(), is(notNullValue()));
        OwnedCoordinates ownedCoordinates = descriptor.getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates, is(notNullValue()));
        MatcherAssert.assertThat(ownedCoordinates.getHostId().getExternalId(), is("the-street"));
        assertThat(ownedCoordinates.getHostAttributeName(), is(expectedHostAttributeName));
    }

    @Test
    void givenACollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldNotBeSaved() {
        when(collectionDescriptorService.retrieveByCoordinates(anyString())).thenReturn(Optional.of(descriptorInDB));

        collectionMakeup.applyCollectionMakeup(streetDto);

        MatcherAssert.assertThat(streetDto.buildings.getId(), is(descriptorInDB.getExternalId()));

        verify(collectionDescriptorService, never()).store(any());
    }

    @Test
    void whenApplyingCollectionMakeup_thenPrivateFieldsAreProcessedToo() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = retrieveNonNullCollectionDescriptor(streetDto.privateBuildings.getId());

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-private-buildings");
    }

    @Test
    void givenADtoHasNullId_whenApplyCollectionMakeup_thenShouldNotChangeAnything() {
        streetDto.setId(null);

        collectionMakeup.applyCollectionMakeup(streetDto);

        MatcherAssert.assertThat(streetDto.buildings.getId(), is(nullValue()));
        verify(collectionDescriptorService, never()).store(any());
    }

    @Test
    void givenADtoHasNullCollectionReference_whenApplyCollectionMakeup_thenShouldNotChangeAnything() {
        streetDto.buildings = null;

        collectionMakeup.applyCollectionMakeup(streetDto);
    }

    @Test
    void givenHostAttributeNameIsNotSpecified_whenApplyingCollectionMakeup_thenHostAttributeNameShouldBeDeducedFromFieldName() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = retrieveNonNullCollectionDescriptor(
                streetDto.buildingsWithDefaultName.getId());

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "buildingsWithDefaultName");
    }

    @Test
    void whenMakeupIsApplied_thenUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        String collectionId = streetDto.buildings.getId();
        MatcherAssert.assertThat(streetDto.buildings.getUrl(), is("https://example.com/collection/" + collectionId));
    }

    @Test
    void givenACollectionIsAnnotatedOnAGetter_whenMakeupIsApplied_thenIdAndUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        MatcherAssert.assertThat(streetDto.getBuildingsAnnotatedViaGetter().getId(), is(notNullValue()));
        String collectionId = streetDto.getBuildingsAnnotatedViaGetter().getId();
        MatcherAssert.assertThat(streetDto.getBuildingsAnnotatedViaGetter().getUrl(),
                is("https://example.com/collection/" + collectionId));
    }

    @Test
    void givenACollectionIsInsideANestedDto_whenMakeupIsApplied_thenInternalIdShouldBeSavedAsHostId() {
        collectionMakeup.applyCollectionMakeup(outerDto);

        String collectionDescriptorId = outerDto.innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        MatcherAssert.assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    @Test
    void givenACollectionIsInsideA2LevelNestedDto_whenMakeupIsApplied_thenInternalIdShouldBeSavedAsHostId() {
        ContainerResponseDto top = new ContainerResponseDto("top-id", outerDto);

        collectionMakeup.applyCollectionMakeup(top);

        String collectionDescriptorId = outerDto.innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        MatcherAssert.assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }
    
    @Test
    void givenACollectionIsInsideANestedDtoViaObjectTypedField_whenMakeupIsApplied_thenTheMakeupShouldBeApplied() {
        InnerResponseDto innerDto = (InnerResponseDto) outerResponseDtoWithObjectTyping.innerDto;

        collectionMakeup.applyCollectionMakeup(outerResponseDtoWithObjectTyping);

        String collectionDescriptorId = innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        MatcherAssert.assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    private static class BuildingResponseDto extends AbstractResponseDto {
        public String address;

        BuildingResponseDto(String externalId, String address) {
            setId(new Descriptor(externalId));
            this.address = address;
        }
    }

    public static class StreetResponseDto extends AbstractResponseDto {
        @OwnedCollection(hostAttributeName = "the-buildings")
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildings;
        @OwnedCollection(hostAttributeName = "the-private-buildings")
        private CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> privateBuildings;
        @OwnedCollection
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildingsWithDefaultName;
        private CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildingsAnnotatedViaGetter;

        StreetResponseDto(String externalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(externalId));
            this.buildings = new CollectionReference<>(buildings);
            this.privateBuildings = new CollectionReference<>(buildings);
            this.buildingsWithDefaultName = new CollectionReference<>(buildings);
            this.buildingsAnnotatedViaGetter = new CollectionReference<>(buildings);
        }

        @OwnedCollection
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> getBuildingsAnnotatedViaGetter() {
            return buildingsAnnotatedViaGetter;
        }
    }

    public static class InnerResponseDto extends AbstractResponseDto {
        @OwnedCollection(hostAttributeName = "the-buildings")
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildings;

        InnerResponseDto(String externalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(externalId));
            this.buildings = new CollectionReference<>(buildings);
        }
    }

    public static class OuterResponseDto extends AbstractResponseDto {
        public InnerResponseDto innerDto;

        OuterResponseDto(String outerExternalId, String innerExternalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(outerExternalId));
            innerDto = new InnerResponseDto(innerExternalId, buildings);
        }
    }

    public static class OuterResponseDtoWithObjectTyping extends AbstractResponseDto {
        public Object innerDto;

        OuterResponseDtoWithObjectTyping(String outerExternalId, String innerExternalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(outerExternalId));
            innerDto = new InnerResponseDto(innerExternalId, buildings);
        }
    }

    public static class ContainerResponseDto extends AbstractResponseDto {
        public ResponseDto dto;

        ContainerResponseDto(String id, ResponseDto dto) {
            setId(new Descriptor(id));
            this.dto = dto;
        }
    }
}