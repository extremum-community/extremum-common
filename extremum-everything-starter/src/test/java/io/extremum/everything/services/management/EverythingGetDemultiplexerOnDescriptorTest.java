package io.extremum.everything.services.management;

import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EverythingGetDemultiplexerOnDescriptorTest {
    @InjectMocks
    private EverythingGetDemultiplexerOnDescriptor multiplexer;

    @Mock
    private EverythingEverythingManagementService everythingManagementService;
    @Mock
    private EverythingCollectionManagementService collectionManagementService;

    @Mock
    private ResponseDto responseDto;

    private final Descriptor singleDescriptor = new Descriptor("external-id");
    private final Descriptor collectionDescriptor = Descriptor.forCollection("external-id",
            CollectionDescriptor.forOwned(new Descriptor("host-id"), "items")
    );

    @Test
    void givenDexcriptorOfTypeSingle_whenGetting_thenEverythingGetShouldBeMade() {
        when(everythingManagementService.get(same(singleDescriptor), anyBoolean()))
                .thenReturn(responseDto);

        Response response = multiplexer.get(singleDescriptor, Projection.empty(), false);

        assertThat(response.getResult(), is(sameInstance(responseDto)));
    }

    @Test
    void givenDexcriptorOfTypeCollection_whenGetting_thenCollectionShouldBeFetched() {
        Response collectionOk = Response.ok();
        when(collectionManagementService.fetchCollection(same(collectionDescriptor), any(), anyBoolean()))
                .thenReturn(collectionOk);

        Response response = multiplexer.get(collectionDescriptor, Projection.empty(), false);

        assertThat(response, is(sameInstance(collectionOk)));
    }
}