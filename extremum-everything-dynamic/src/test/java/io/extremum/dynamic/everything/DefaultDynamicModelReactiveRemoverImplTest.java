package io.extremum.dynamic.everything;

import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

class DefaultDynamicModelReactiveRemoverImplTest {
    @Test
    void remove() {
        JsonBasedDynamicModelService dynamicModelService = mock(JsonBasedDynamicModelService.class);
        ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities = mock(ReactiveMongoDescriptorFacilities.class);

        DefaultDynamicModelReactiveRemoverImpl remover = new DefaultDynamicModelReactiveRemoverImpl(
                dynamicModelService,
                reactiveMongoDescriptorFacilities
        );

        final String internalId = "5e035f6c3e18440951496a9c";

        final Descriptor descr = Descriptor.builder()
                .internalId(internalId)
                .externalId("2ec7e347-f172-43af-8c90-aa6df6175e6b")
                .build();

        doAnswer(invocation -> {
            ObjectId id = (ObjectId) invocation.getArguments()[0];

            if (internalId.equals(id.toString())) {
                return just(descr);
            } else {
                return fail("Unexpected id. Expected " + internalId + " actual " + id.toString());
            }
        }).when(reactiveMongoDescriptorFacilities).fromInternalId(any());

        doReturn(empty())
                .when(dynamicModelService).remove(descr);

        Mono<Void> result = remover.remove(internalId);

        StepVerifier.create(result).verifyComplete();

        verify(dynamicModelService).remove(descr);
    }
}