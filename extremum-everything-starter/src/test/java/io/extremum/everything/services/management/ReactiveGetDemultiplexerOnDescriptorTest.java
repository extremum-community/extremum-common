package io.extremum.everything.services.management;

import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveGetDemultiplexerOnDescriptorTest {
    @InjectMocks
    private ReactiveGetDemultiplexerOnDescriptor demultiplexer;

    @Mock
    private ReactiveEverythingManagementService everythingManagementService;
    @Mock
    private EverythingCollectionManagementService collectionManagementService;

    @Mock
    private ResponseDto responseDto;

    private final Descriptor singleDescriptor = Descriptor.builder()
            .externalId("external-id")
            .type(Descriptor.Type.SINGLE)
            .build();
    private final Descriptor collectionDescriptor = Descriptor.forCollection("external-id",
            CollectionDescriptor.forOwned(new Descriptor("host-id"), "items")
    );

    private DescriptorLoader oldDescriptorLoader;

    @Mock
    private DescriptorLoader descriptorLoader;

    @BeforeEach
    void initDescriptorLoader() {
        //noinspection deprecation
        oldDescriptorLoader = StaticDescriptorLoaderAccessor.getDescriptorLoader();
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

    @AfterEach
    void restoreDescriptorLoader() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(oldDescriptorLoader);
    }

    @Test
    void givenDescriptorOfTypeSingle_whenGetting_thenEverythingGetShouldBeMade() {
        when(everythingManagementService.get(same(singleDescriptor), anyBoolean()))
                .thenReturn(Mono.just(responseDto));

        Response response = demultiplexer.get(singleDescriptor, Projection.empty(), false).block();

        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(sameInstance(responseDto)));
    }

    @Test
    void givenDescriptorOfTypeCollection_whenGetting_thenCollectionShouldBeFetched() {
        Response collectionOk = Response.ok();
        when(collectionManagementService.fetchCollectionReactively(same(collectionDescriptor), any(), anyBoolean()))
                .thenReturn(Mono.just(collectionOk));

        Response response = demultiplexer.get(collectionDescriptor, Projection.empty(), false).block();

        assertThat(response, is(sameInstance(collectionOk)));
    }

    @Test
    void givenDescriptorLoadsWithTypeSingle_whenGetting_thenEverythingGetShouldBeMade() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(singleDescriptor));
        when(everythingManagementService.get(any(), anyBoolean()))
                .thenReturn(Mono.just(responseDto));

        Response response = demultiplexer.get(new Descriptor("external-id"), Projection.empty(), false).block();

        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(sameInstance(responseDto)));
    }

    @Test
    void givenDescriptorLoadsWithTypeCollection_whenGetting_thenCollectionShouldBeFetched() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(collectionDescriptor));
        Response collectionOk = Response.ok();
        when(collectionManagementService.fetchCollectionReactively(any(), any(), anyBoolean()))
                .thenReturn(Mono.just(collectionOk));

        Response response = demultiplexer.get(new Descriptor("external-id"), Projection.empty(), false).block();

        assertThat(response, is(sameInstance(collectionOk)));
    }
}