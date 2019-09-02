package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.urls.TestApplicationUrls;
import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
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
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class CollectionMakeupImplTest {
    @InjectMocks
    private CollectionMakeupImpl collectionMakeup;

    @Spy
    private InMemoryDescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private DescriptorSaver descriptorSaver = new DescriptorSaver(descriptorService);
    @Spy
    private CollectionDescriptorService collectionDescriptorService = new InMemoryCollectionDescriptorService(
            descriptorService);
    @Spy
    private ApplicationUrls applicationUrls = new TestApplicationUrls();

    private StreetResponseDto streetDto;
    private final Descriptor descriptorInDB = Descriptor.forCollection("external-id",
            CollectionDescriptor.forOwned(
                    new Descriptor("the-street"), "the-buildings")
    );
    {
        descriptorInDB.setExternalId(UUID.randomUUID().toString());
    }
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
        CollectionDescriptor descriptor = retrieveNonNullSavedCollectionDescriptor(collectionId);

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-buildings");
    }

    @NotNull
    private CollectionDescriptor retrieveNonNullSavedCollectionDescriptor(String collectionId) {
        assertThat(collectionId, is(notNullValue()));

        Descriptor descriptor = descriptorService.loadByExternalId(collectionId)
                .orElse(null);
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is(notNullValue()));
        assertThat(descriptor.getType(), is(Descriptor.Type.COLLECTION));

        CollectionDescriptor collectionDescriptor = descriptor.getCollection();
        assertThat(collectionDescriptor, is(notNullValue()));
        return collectionDescriptor;
    }

    private void assertThatStreetBuildingsCollectionGotMakeupApplied(CollectionDescriptor descriptor,
            String expectedHostAttributeName) {
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getCoordinates(), is(notNullValue()));
        OwnedCoordinates ownedCoordinates = descriptor.getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates, is(notNullValue()));
        assertThat(ownedCoordinates.getHostId().getExternalId(), is("the-street"));
        assertThat(ownedCoordinates.getHostAttributeName(), is(expectedHostAttributeName));
    }

    @Test
    void givenACollectionDescriptorExists_whenApplyingCollectionMakeup_thenCollectionDescriptorShouldNotBeSaved() {
        when(collectionDescriptorService.retrieveByCoordinates(anyString())).thenReturn(Optional.of(descriptorInDB));

        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.buildings.getId(), is(descriptorInDB.getExternalId()));

        verify(descriptorService, never()).store(any());
    }

    @Test
    void whenApplyingCollectionMakeup_thenPrivateFieldsAreProcessedToo() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = retrieveNonNullSavedCollectionDescriptor(streetDto.privateBuildings.getId());

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "the-private-buildings");
    }

    @Test
    void givenADtoHasNullId_whenApplyCollectionMakeup_thenShouldNotChangeAnything() {
        streetDto.setId(null);

        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.buildings.getId(), is(nullValue()));
        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenADtoHasNullCollectionReference_whenApplyCollectionMakeup_thenShouldNotChangeAnything() {
        streetDto.buildings = null;

        collectionMakeup.applyCollectionMakeup(streetDto);
    }

    @Test
    void givenHostAttributeNameIsNotSpecified_whenApplyingCollectionMakeup_thenHostAttributeNameShouldBeDeducedFromFieldName() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        CollectionDescriptor descriptor = retrieveNonNullSavedCollectionDescriptor(
                streetDto.buildingsWithDefaultName.getId());

        assertThatStreetBuildingsCollectionGotMakeupApplied(descriptor, "buildingsWithDefaultName");
    }

    @Test
    void whenMakeupIsApplied_thenUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        String collectionId = streetDto.buildings.getId();
        assertThat(streetDto.buildings.getUrl(), is("https://example.com/" + collectionId));
    }

    @Test
    void givenACollectionIsAnnotatedOnAGetter_whenMakeupIsApplied_thenIdAndUrlShouldBeFilled() {
        collectionMakeup.applyCollectionMakeup(streetDto);

        assertThat(streetDto.getBuildingsAnnotatedViaGetter().getId(), is(notNullValue()));
        String collectionId = streetDto.getBuildingsAnnotatedViaGetter().getId();
        assertThat(streetDto.getBuildingsAnnotatedViaGetter().getUrl(),
                is("https://example.com/" + collectionId));
    }

    @Test
    void givenACollectionIsInsideANestedDto_whenMakeupIsApplied_thenInternalIdShouldBeSavedAsHostId() {
        collectionMakeup.applyCollectionMakeup(outerDto);

        String collectionDescriptorId = outerDto.innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullSavedCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    @Test
    void givenACollectionIsInsideA2LevelNestedDto_whenMakeupIsApplied_thenInternalIdShouldBeSavedAsHostId() {
        ContainerResponseDto top = new ContainerResponseDto("top-id", outerDto);

        collectionMakeup.applyCollectionMakeup(top);

        String collectionDescriptorId = outerDto.innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullSavedCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }
    
    @Test
    void givenACollectionIsInsideANestedDtoViaObjectTypedField_whenMakeupIsApplied_thenTheMakeupShouldBeApplied() {
        InnerResponseDto innerDto = (InnerResponseDto) outerResponseDtoWithObjectTyping.innerDto;

        collectionMakeup.applyCollectionMakeup(outerResponseDtoWithObjectTyping);

        String collectionDescriptorId = innerDto.buildings.getId();
        CollectionDescriptor collectionDescriptor = retrieveNonNullSavedCollectionDescriptor(collectionDescriptorId);

        OwnedCoordinates coordinates = collectionDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(coordinates.getHostId().getExternalId(), is("inner-id"));
        assertThat(coordinates.getHostAttributeName(), is("the-buildings"));
    }

    private static class BuildingResponseDto extends CommonResponseDto {
        public String address;

        BuildingResponseDto(String externalId, String address) {
            setId(new Descriptor(externalId));
            this.address = address;
        }

        @Override
        public String getModel() {
            return "Building";
        }
    }

    public static class StreetResponseDto extends CommonResponseDto {
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

        @Override
        public String getModel() {
            return "Street";
        }
    }

    public static class InnerResponseDto extends CommonResponseDto {
        @OwnedCollection(hostAttributeName = "the-buildings")
        public CollectionReference<IdOrObject<Descriptor, BuildingResponseDto>> buildings;

        InnerResponseDto(String externalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(externalId));
            this.buildings = new CollectionReference<>(buildings);
        }

        @Override
        public String getModel() {
            return "Inner";
        }
    }

    public static class OuterResponseDto extends CommonResponseDto {
        public InnerResponseDto innerDto;

        OuterResponseDto(String outerExternalId, String innerExternalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(outerExternalId));
            innerDto = new InnerResponseDto(innerExternalId, buildings);
        }

        @Override
        public String getModel() {
            return "Outer";
        }
    }

    public static class OuterResponseDtoWithObjectTyping extends CommonResponseDto {
        public Object innerDto;

        OuterResponseDtoWithObjectTyping(String outerExternalId, String innerExternalId,
                List<IdOrObject<Descriptor, BuildingResponseDto>> buildings) {
            setId(new Descriptor(outerExternalId));
            innerDto = new InnerResponseDto(innerExternalId, buildings);
        }

        @Override
        public String getModel() {
            return "OuterWithObjectTyping";
        }
    }

    public static class ContainerResponseDto extends CommonResponseDto {
        public ResponseDto dto;

        ContainerResponseDto(String id, ResponseDto dto) {
            setId(new Descriptor(id));
            this.dto = dto;
        }

        @Override
        public String getModel() {
            return "Container";
        }
    }
}