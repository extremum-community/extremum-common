package io.extremum.common.collection.service;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.dao.ReactiveCollectionDescriptorDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveCollectionDescriptorServiceImplTest {
    @InjectMocks
    private ReactiveCollectionDescriptorServiceImpl service;

    @Mock
    private ReactiveCollectionDescriptorDao reactiveCollectionDescriptorDao;

    private final CollectionDescriptor collectionDescriptor = new CollectionDescriptor("externalId");

    @Test
    void whenRetrievingACollection_thenItShouldBeRetrievedFromDao() {
        when(reactiveCollectionDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collectionDescriptor));

        Mono<CollectionDescriptor> mono = service.retrieveByExternalId("externalId");

        assertThat(mono.block(), is(sameInstance(collectionDescriptor)));
    }
}