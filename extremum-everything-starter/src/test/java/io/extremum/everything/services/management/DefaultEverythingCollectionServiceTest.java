package io.extremum.everything.services.management;

import com.google.common.collect.ImmutableList;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.conversion.OwnedCollection;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
import io.extremum.common.reactive.NaiveReactifier;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
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
import org.jetbrains.annotations.NotNull;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    @Spy
    private CollectionTransactivity transactivity = new TestCollectionTransactivity();
    @Spy
    private Reactifier reactifier = new NaiveReactifier();

    private static final ObjectId id1 = new ObjectId();
    private static final ObjectId id2 = new ObjectId();

    @BeforeEach
    void setUp() {
        service = new DefaultEverythingCollectionService(
                new ModelRetriever(ImmutableList.of(streetGetterService), null),
                Collections.singletonList(new ExplicitHouseFetcher()),
                Collections.singletonList(new ExplicitHouseStreamer()),
                dtoConversionService,
                universalDao, reactifier, transactivity
        );
    }

    @Test
    void givenHostExistsAndNoCollectionFetcherRegistered_whenCollectionIsFetched_thenItShouldBeReturned() {
        returnStreetAndHousesAndConvertToDtosInBlockingMode();

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos.elements(), hasSize(2));
    }

    private void returnStreetWhenRequested() {
        when(streetGetterService.get("internalHostId")).thenReturn(new Street());
    }

    private void retrieve2HousesWhenRequestedByIds() {
        when(universalDao.retrieveByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(CollectionFragment.forCompleteCollection(Arrays.asList(new House(), new House())));
    }

    private void convertToResponseDtoWhenRequested() {
        when(dtoConversionService.convertUnknownToResponseDto(any(), any()))
                .thenReturn(mock(ResponseDto.class));
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

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
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
    void givenHostExistsAndNoCollectionStreamerRegistered_whenCollectionIsStreamed_thenItShouldBeReturned() {
        returnStreetReactivelyWhenRequested();
        stream2HousesWhenRequestedByIds();
        convertToResponseDtoReactivelyWhenRequested();

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
        Projection projection = Projection.empty();

        Flux<ResponseDto> dtos = service.streamCollection(collectionDescriptor, projection, false);

        assertThat(dtos.toStream().collect(Collectors.toList()), hasSize(2));
    }

    private void stream2HousesWhenRequestedByIds() {
        when(universalDao.streamByIds(eq(Arrays.asList(id1, id2)), eq(House.class), any()))
                .thenReturn(Flux.just(new House(), new House()));
    }

    private void returnStreetReactivelyWhenRequested() {
        when(streetGetterService.reactiveGet("internalHostId")).thenReturn(Mono.just(new Street()));
    }

    private void convertToResponseDtoReactivelyWhenRequested() {
        when(dtoConversionService.convertUnknownToResponseDtoReactively(any(), any()))
                .thenReturn(Mono.just(mock(ResponseDto.class)));
    }

    @Test
    void givenHostDoesNotExist_whenCollectionIsStreamed_thenAnExceptionShouldBeThrown() {
        when(streetGetterService.reactiveGet("internalHostId")).thenReturn(Mono.empty());

        CollectionDescriptor collectionDescriptor = housesCollectionDescriptor();
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
        convertToResponseDtoReactivelyWhenRequested();

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(streetDescriptor(),
                "explicitHouses");
        Projection projection = Projection.empty();

        Flux<ResponseDto> houses = service.streamCollection(collectionDescriptor, projection, false);

        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(1));
    }

    @Test
    void givenHostExistsAndTransactivityRequiresExecuteInATransaction_whenCollectionIsStreamed_thenItShouldStreamed() {
        when(transactivity.isCollectionTransactional(any())).thenReturn(true);
        returnStreetWhenRequested();
        retrieve2HousesWhenRequestedByIds();
        convertToResponseDtoReactivelyWhenRequested();

        Flux<ResponseDto> dtos = service.streamCollection(housesCollectionDescriptor(), Projection.empty(), false);

        assertThat(dtos.toStream().collect(Collectors.toList()), hasSize(2));
    }

    private void returnStreetAndHousesAndConvertToDtosInBlockingMode() {
        returnStreetWhenRequested();
        retrieve2HousesWhenRequestedByIds();
        convertToResponseDtoWhenRequested();
    }

    @NotNull
    private CollectionDescriptor housesCollectionDescriptor() {
        Descriptor hostId = streetDescriptor();
        return CollectionDescriptor.forOwned(hostId, "houses");
    }

    @Test
    void givenHostExistsAndTransactivityRequiresExecuteInATransaction_whenCollectionIsStreamed_thenItShouldBeFetchedInsideATransactionAndReactified() {
        when(transactivity.isCollectionTransactional(any())).thenReturn(true);
        returnStreetWhenRequested();
        retrieve2HousesWhenRequestedByIds();
        convertToResponseDtoReactivelyWhenRequested();

        Flux<ResponseDto> dtos = service.streamCollection(housesCollectionDescriptor(), Projection.empty(), false);
        dtos.blockLast();

        verify(transactivity).doInTransaction(any(), any());
        //noinspection UnassignedFluxMonoInstance
        verify(reactifier).flux(any());
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
        @SuppressWarnings("unused")
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

    private static class TestCollectionTransactivity implements CollectionTransactivity {
        @Override
        public boolean isCollectionTransactional(Descriptor hostId) {
            return false;
        }

        @Override
        public <T> T doInTransaction(Descriptor hostId, Supplier<T> action) {
            return action.get();
        }
    }
}