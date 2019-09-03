package io.extremum.common.descriptor.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
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
class ReactiveDescriptorServiceImplTest {
    @InjectMocks
    private ReactiveDescriptorServiceImpl reactiveDescriptorService;

    @Mock
    private ReactiveDescriptorDao reactiveDescriptorDao;

    private final Descriptor descriptor = new Descriptor("external-id");

    @Test
    void whenLoading_thenDescriptorShouldBeLoadedFromDao() {
        when(reactiveDescriptorDao.retrieveByInternalId("internalId")).thenReturn(Mono.just(descriptor));

        Mono<Descriptor> mono = reactiveDescriptorService.loadByInternalId("internalId");

        assertThat(mono.block(), is(sameInstance(descriptor)));
    }
}