package io.extremum.dynamic.everything;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.just;

class DefaultReactiveDynamicModelGetterTest {
    @Test
    void get() {
        JsonBasedDynamicModelService dynamicModelService = mock(JsonBasedDynamicModelService.class);
        ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities = mock(ReactiveMongoDescriptorFacilities.class);

        DefaultReactiveDynamicModelGetter getter = new DefaultReactiveDynamicModelGetter(
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

        doReturn(just(mock(JsonDynamicModel.class)))
                .when(dynamicModelService).findById(descr);

        Mono<Model> result = getter.get(internalId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(dynamicModelService).findById(descr);
    }

    @Test
    void get_emptyMono_if_modelNotFound() {
        JsonBasedDynamicModelService dynamicModelService = mock(JsonBasedDynamicModelService.class);
        ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities = mock(ReactiveMongoDescriptorFacilities.class);

        DefaultReactiveDynamicModelGetter getter = new DefaultReactiveDynamicModelGetter(
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

        doReturn(Mono.error(new ModelNotFoundException("not found")))
                .when(dynamicModelService).findById(descr);

        Mono<Model> result = getter.get(internalId);

        StepVerifier.create(result).verifyComplete();

        verify(dynamicModelService).findById(descr);
    }
}