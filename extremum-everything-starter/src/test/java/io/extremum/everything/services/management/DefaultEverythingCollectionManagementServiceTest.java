package io.extremum.everything.services.management;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.everything.services.collection.EverythingCollectionService;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseStatusEnum;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingCollectionManagementServiceTest {
    @InjectMocks
    private DefaultEverythingCollectionManagementService service;

    @Mock
    private EverythingCollectionService everythingCollectionService;
    @Mock
    private ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;

    private final Descriptor collectionDescriptor = Descriptor.forCollection("external-id",
            collectionDescriptor());

    private final Collection<ResponseDto> expectedCollection = Arrays.asList(
            mock(ResponseDto.class), mock(ResponseDto.class));

    private final Projection emptyProjection = Projection.empty();

    @Test
    void givenACollectionDescriptorExists_whenFetchingTheCollection_thenTheCollectionShouldBeReturned() {
        setupMocksToReturnACollection();

        Response response = service.fetchCollection(collectionDescriptor, emptyProjection, true);

        assertThat(response, is(notNullValue()));
        MatcherAssert.assertThat(response.getStatus(), CoreMatchers.is(ResponseStatusEnum.OK));
        assertThat(response.getCode(), is(200));
        assertThat(response.getResult(), is(instanceOf(Collection.class)));
        @SuppressWarnings("unchecked")
        Collection<ResponseDto> collection = (Collection<ResponseDto>) response.getResult();
        assertThat(collection, is(sameInstance(expectedCollection)));
        assertThat(response.getPagination(), is(notNullValue()));
        assertThat(response.getPagination().getCount(), is(2));
    }

    private void setupMocksToReturnACollection() {
        when(everythingCollectionService.fetchCollection(any(), any(), anyBoolean()))
                .thenReturn(CollectionFragment.forCompleteCollection(expectedCollection));
    }

    @NotNull
    private CollectionDescriptor collectionDescriptor() {
        return CollectionDescriptor.forOwned(new Descriptor("host-id"), "items");
    }

    @Test
    void givenACollectionDescriptorExistsWithNullCollection_whenFetchingTheCollection_thenAnExceptionShouldBeThrown() {
        collectionDescriptor.setCollection(null);

        try {
            service.fetchCollection(collectionDescriptor, emptyProjection, true);
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("For 'external-id' no collection was in the descriptor"));
        }
    }

    @Test
    void whenFetchingTheCollectionWithAProjection_thenTheResponsePaginationShouldBeFilledCorrectly() {
        setupMocksToReturnACollection();
        ZonedDateTime since = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime until = ZonedDateTime.of(3000, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        Projection projection = new Projection(1, 11, since, until);

        Response response = service.fetchCollection(collectionDescriptor, projection, true);

        assertThat(response.getPagination(), is(notNullValue()));
        assertThat(response.getPagination().getCount(), is(2));
        assertThat(response.getPagination().getOffset(), is(1));
        assertThat(response.getPagination().getTotal(), is(2L));
        assertThat(response.getPagination().getSince(), is(since));
        assertThat(response.getPagination().getUntil(), is(until));
    }

    @Test
    void whenStreamingACollection_thenItShouldBeStreamed() {
        when(reactiveCollectionDescriptorService.retrieveByExternalId("collection-id"))
                .thenReturn(Mono.just(collectionDescriptor()));
        when(everythingCollectionService.streamCollection(any(), any(), anyBoolean()))
                .thenReturn(Flux.just(expectedCollection.toArray(new ResponseDto[0])));

        Flux<ResponseDto> dtoFlux = service.streamCollection("collection-id", emptyProjection, true);
        List<ResponseDto> dtos = dtoFlux.toStream().collect(Collectors.toList());
        assertThat(dtos, hasSize(2));
    }
}