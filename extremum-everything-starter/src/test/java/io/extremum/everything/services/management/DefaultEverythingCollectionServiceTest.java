package io.extremum.everything.services.management;

import com.google.common.collect.ImmutableList;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.conversion.OwnedCollection;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
import io.extremum.everything.collection.CollectionElementType;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.CollectionFetcher;
import io.extremum.everything.services.CollectionStreamer;
import io.extremum.everything.services.GetterService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingCollectionServiceTest {
    @InjectMocks
    private DefaultEverythingCollectionService service;

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
        service = new DefaultEverythingCollectionService(
                new ModelRetriever(ImmutableList.of(streetGetterService), null),
                Collections.singletonList(new ExplicitHouseFetcher()),
                Collections.singletonList(new ExplicitHouseStreamer()),
                dtoConversionService,
                universalDao
        );
    }

    @Test
    void givenHostExistsAndNoCollectionFetcherRegistered_whenCollectionIsFetched_thenItShouldBeReturned() {
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
            fail("An exception should be thrown");
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
    void givenHostExistsAndNoCollectionFetcherRegistered_whenCollectionIsStreamed_thenItShouldBeReturned() {
        when(streetGetterService.reactiveGet("internalHostId")).thenReturn(Mono.just(new Street()));
        when(universalDao.retrieveByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(CollectionFragment.forCompleteCollection(Arrays.asList(new House(), new House())));
        // TODO: uncomment this
//        when(universalDao.streamByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
//                .thenReturn(Flux.just(new House(), new House()));
        convertToResponseDtoWhenRequested();

        Descriptor hostId = streetDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "houses");
        Projection projection = Projection.empty();

        Flux<ResponseDto> dtos = service.streamCollection(collectionDescriptor, projection, false);

        assertThat(dtos.toStream().collect(Collectors.toList()), hasSize(2));
    }

    @Test
    void givenHostDoesNotExist_whenCollectionIsStreamed_thenAnExceptionShouldBeThrown() {
        when(streetGetterService.reactiveGet("internalHostId")).thenReturn(Mono.empty());

        Descriptor hostId = streetDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "houses");
        Projection projection = Projection.empty();

        AtomicReference<Throwable> throwableRef = new AtomicReference<>();

        service.streamCollection(collectionDescriptor, projection, false)
                .doOnError(throwableRef::set)
                .subscribe();

        assertThat(throwableRef.get(), is(notNullValue()));
        assertThat(throwableRef.get().getMessage(), is("No host entity was found by external ID 'hostId'"));
    }

    @Test
    void givenAnExplicitCollectionFetcherIsDefined_whenCollectionIsStreamed_thenItShouldBeProvidedByTheFetcher() {
        convertToResponseDtoWhenRequested();

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(streetDescriptor(),
                "explicitHouses");
        Projection projection = Projection.empty();

        Flux<ResponseDto> houses = service.streamCollection(collectionDescriptor, projection, false);

        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(1));
    }

    @ModelName("House")
    private static class House extends MongoCommonModel {
    }

    @SuppressWarnings("WeakerAccess")
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

    private static class ExplicitHouseStreamer implements CollectionStreamer<Street, House> {

        @Override
        public String getHostAttributeName() {
            return "explicitHouses";
        }

        @Override
        public Flux<House> streamCollection(Street street, Projection projection) {
            return Flux.just(new House());
        }

        @Override
        public String getSupportedModel() {
            return "Street";
        }
    }

}