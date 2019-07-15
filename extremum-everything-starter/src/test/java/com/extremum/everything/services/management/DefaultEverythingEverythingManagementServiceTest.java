package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.conversion.OwnedCollection;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.collection.CollectionElementType;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.security.AllowEverythingForDataAccess;
import com.extremum.everything.security.AllowEverythingForRoleAccess;
import com.extremum.everything.services.CollectionFetcher;
import com.extremum.everything.services.GetterService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingEverythingManagementServiceTest {
    @InjectMocks
    private DefaultEverythingEverythingManagementService service;

    @Spy
    private GetterService<Street> streetGetterService = new StreetGetter();
    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;

    private static final ObjectId id1 = new ObjectId();
    private static final ObjectId id2 = new ObjectId();

    @BeforeEach
    void setUp() {
        service = new DefaultEverythingEverythingManagementService(
                ImmutableList.of(streetGetterService, new AlwaysNullGetterService()),
                Collections.emptyList(),
                Collections.emptyList(),
                null, null, null,
                Collections.singletonList(new ExplicitHouseFetcher()),
                dtoConversionService,
                universalDao,
                new AllowEverythingForRoleAccess(),
                new AllowEverythingForDataAccess()
        );
    }

    @Test
    void givenHostExists_whenCollectionIsFetched_thenItShouldBeReturned() {
        returnStreetWhenRequested();
        when(universalDao.retrieveByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(CollectionFragment.forCompleteCollection(Arrays.asList(new House(), new House())));
        convertToResponseDtoWhenRequested();

        Descriptor hostId = streetDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "houses");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos.elements(), hasSize(2));
    }

    private void convertToResponseDtoWhenRequested() {
        when(dtoConversionService.convertUnknownToResponseDto(any(), any()))
                .thenReturn(mock(ResponseDto.class));
    }

    private void returnStreetWhenRequested() {
        when(streetGetterService.get("internalHostId")).thenReturn(new Street());
    }

    private Descriptor streetDescriptor() {
        return Descriptor.builder()
                .externalId("hostId")
                .internalId("internalHostId")
                .modelType("Street")
                .storageType(Descriptor.StorageType.MONGO)
                .build();
    }

    @Test
    void givenHostDoesNotExist_whenCollectionIsFetched_thenAnExceptionShouldBeThrown() {
        when(streetGetterService.get("internalHostId")).thenReturn(null);

        Descriptor hostId = streetDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "houses");
        Projection projection = Projection.empty();

        try {
            service.fetchCollection(collectionDescriptor, projection, false);
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No host entity was found by external ID 'hostId'"));
        }
    }

    @Test
    void givenAnExplicitCollectionFetcherIsDefined_whenCollectionIsFetched_thenItShouldBeProvidedByTheFetcher() {
        convertToResponseDtoWhenRequested();

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(streetDescriptor(),
                "explicitHouses");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> houses = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(houses.elements(), hasSize(1));
    }

    @Test
    void givenGetterServiceReturnsNull_whenGetting_thenModelNotFoundExceptionShouldBeThrown() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .modelType("AlwaysNull")
                .build();
        try {
            service.get(descriptor, false);
            fail("A ModelNotFoundException is expected");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is("Nothing was found by 'external-id'"));
        }
    }

    @ModelName("House")
    private static class House extends MongoCommonModel {
    }

    @ModelName("Street")
    @Getter
    public static class Street extends MongoCommonModel {
        @OwnedCollection
        @CollectionElementType(House.class)
        private List<String> houses = Arrays.asList(id1.toString(), id2.toString());
        @OwnedCollection
        private List<String> explicitHouses;
    }

    private static class StreetGetter implements GetterService<Street> {
        @Override
        public Street get(String id) {
            return new Street();
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    private static class ExplicitHouseFetcher implements CollectionFetcher<Street, House> {

        @Override
        public String getHostAttributeName() {
            return "explicitHouses";
        }

        @Override
        public CollectionFragment<House> fetchCollection(Street street, Projection projection) {
            return CollectionFragment.forCompleteCollection(Collections.singletonList(new House()));
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    private static class AlwaysNullGetterService implements GetterService<Model> {
        @Override
        public Model get(String id) {
            return null;
        }

        @Override
        public String getSupportedModel() {
            return "AlwaysNull";
        }
    }
}