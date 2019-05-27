package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.conversion.OwnedCollection;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.collection.CollectionElementType;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.services.CollectionFetcher;
import com.extremum.everything.services.GetterService;
import com.extremum.everything.services.management.config.TestConfig;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
    @Spy
    private GetterService<JpaBasicContainer> jpaBasicContainerGetterService = new JpaBasicContainerGetter();
    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;

    private static final ObjectId id1 = new ObjectId();
    private static final ObjectId id2 = new ObjectId();
    private static ConfigurableApplicationContext context;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        context.start();
    }

    @AfterAll
    static void clear() {
        context.stop();
    }

    @BeforeEach
    void setUp() {
        service = new DefaultEverythingEverythingManagementService(
                Arrays.asList(streetGetterService, jpaBasicContainerGetterService),
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(new ExplicitHouseFetcher(), new ExplicitJpaBasicElementFetcher()),
                dtoConversionService,
                universalDao
        );
    }

    @Test
    void givenHostExists_whenCollectionIsFetched_thenItShouldBeReturned() {
        returnStreetWhenRequested();
        when(universalDao.retrieveByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(Arrays.asList(new House(), new House()));
        convertToResponseDtoWhenRequested();

        Descriptor hostId = streetDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "houses");
        Projection projection = Projection.empty();

        Collection<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos, hasSize(2));
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

        Collection<ResponseDto> houses = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(houses, hasSize(1));
    }

    @Test
    void givenAJpaBasicContainerExists_whenACollectionIsFetched_thenItShouldBeReturned() {
        when(jpaBasicContainerGetterService.get("internalHostId")).thenReturn(new JpaBasicContainer());

        Descriptor hostId = jpaBasicContainerDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "elements");
        Projection projection = Projection.empty();

        Collection<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos, hasSize(2));
    }

    private Descriptor jpaBasicContainerDescriptor() {
        return Descriptor.builder()
                .externalId("hostId")
                .internalId("internalHostId")
                .modelType("JpaBasicContainer")
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();
    }

    @Test
    void givenAnExplicitJpaBasicCollectionFetcherIsDefined_whenCollectionIsFetched_thenItShouldBeProvidedByTheFetcher() {
        convertToResponseDtoWhenRequested();

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(jpaBasicContainerDescriptor(),
                "explicitElements");
        Projection projection = Projection.empty();

        Collection<ResponseDto> elements = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(elements, hasSize(1));
    }

    @ModelName("House")
    private static class House extends MongoCommonModel {
    }

    @ModelName("Street")
    private static class Street extends MongoCommonModel {
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
        public String getHostFieldName() {
            return "explicitHouses";
        }

        @Override
        public List<House> fetchCollection(Street street, Projection projection) {
            return Collections.singletonList(new House());
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

    @ModelName("JpaBasicElement")
    private static class JpaBasicElement extends PostgresBasicModel {
    }

    @ModelName("JpaBasicContainer")
    private static class JpaBasicContainer extends PostgresBasicModel {
        @OwnedCollection
        private List<JpaBasicElement> elements = Arrays.asList(new JpaBasicElement(), new JpaBasicElement());
        @OwnedCollection
        private List<JpaBasicElement> explicitElements;
    }

    private static class JpaBasicContainerGetter implements GetterService<JpaBasicContainer> {
        @Override
        public JpaBasicContainer get(String id) {
            return new JpaBasicContainer();
        }

        @Override
        public String getSupportedModel() {
            return "JpaBasicContainer";
        }
    }

    private static class ExplicitJpaBasicElementFetcher implements CollectionFetcher<JpaBasicContainer,
            JpaBasicElement> {

        @Override
        public String getHostFieldName() {
            return "explicitElements";
        }

        @Override
        public List<JpaBasicElement> fetchCollection(JpaBasicContainer container, Projection projection) {
            return Collections.singletonList(new JpaBasicElement());
        }

        @Override
        public String getSupportedModel() {
            return "JpaBasicContainer";
        }
    }
}