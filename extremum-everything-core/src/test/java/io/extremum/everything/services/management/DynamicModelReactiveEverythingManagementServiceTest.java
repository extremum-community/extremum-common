package io.extremum.everything.services.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.everything.services.defaultservices.DefaultDynamicModelReactiveRemover;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;
import static reactor.test.StepVerifier.create;

class DynamicModelReactiveEverythingManagementServiceTest {
    Descriptor modelDescriptor = Descriptor.builder()
            .internalId("internal-id")
            .externalId("external-id")
            .modelType("DynamicModel-A")
            .build();

    @Test
    void get() {
        ReactiveDataSecurity dataSecurity = mock(ReactiveDataSecurity.class);
        ReactiveDynamicModelGetter modelGetter = mock(ReactiveDynamicModelGetter.class);
        DynamicModelDtoConversionService dtoConversionService = mock(DynamicModelDtoConversionService.class);

        DynamicModelReactiveEverythingManagementService service = new DynamicModelReactiveEverythingManagementService(
                null,
                null,
                dataSecurity,
                modelGetter,
                dtoConversionService,
                null
        );

        // configure behaviour
        Model model = mock(Model.class);
        ResponseDto modelResponseDto = new ResponseDtoTest(modelDescriptor);

        doReturn(empty())
                .when(dataSecurity).checkGetAllowed(model);
        doReturn(just(model))
                .when(modelGetter).get(modelDescriptor.getInternalId());
        doReturn(just(modelResponseDto))
                .when(dtoConversionService).convertToResponseDtoReactively(eq(model), any());

        // call service operation
        Mono<ResponseDto> result = service.get(modelDescriptor, false);

        // launch a reactive pipe & verifying result
        create(result)
                .assertNext(responseDto -> assertEquals(modelDescriptor, responseDto.getId()))
                .verifyComplete();

        // verifying behaviour
        verify(dataSecurity).checkGetAllowed(model);
        verify(modelGetter).get(modelDescriptor.getInternalId());
        verify(dtoConversionService).convertToResponseDtoReactively(eq(model), any());
    }

    @Test
    void patch() throws JsonPatchException {
        ModelRetriever modelRetriever = mock(ModelRetriever.class);
        ReactivePatcher patcher = mock(ReactivePatcher.class);
        ReactiveModelSaver modelSaver = mock(ReactiveModelSaver.class);
        ReactiveDataSecurity dataSecurity = mock(ReactiveDataSecurity.class);
        PatcherHooksCollection hooksCollection = mock(PatcherHooksCollection.class);

        ReactivePatchFlow patchFlowOrigin = new ReactivePatchFlowImpl(
                modelRetriever,
                patcher,
                modelSaver,
                dataSecurity,
                hooksCollection
        );

        ReactivePatchFlow patchFlow = spy(patchFlowOrigin);

        DynamicModelDtoConversionService dynamicModelConversionService = mock(DynamicModelDtoConversionService.class);

        DynamicModelReactiveEverythingManagementService service = new DynamicModelReactiveEverythingManagementService(
                null,
                patchFlow,
                null,
                null,
                dynamicModelConversionService,
                null
        );

        // configure behaviour
        JsonPatch patch = mock(JsonPatch.class);
        Model model = mock(Model.class);
        JsonNode patchingNode = mock(JsonNode.class);
        ResponseDto responseDto = new ResponseDtoTest(modelDescriptor);

        doReturn(just(model))
                .when(modelRetriever).retrieveModelReactively(modelDescriptor);
        doReturn(just(model))
                .when(patcher).patch(modelDescriptor, model, patch);
        doReturn(patchingNode)
                .when(patch).apply(patchingNode);
        doReturn(just(model))
                .when(modelSaver).saveModel(model);
        doReturn(empty())
                .when(dataSecurity).checkPatchAllowed(model);
        doReturn(just(responseDto))
                .when(dynamicModelConversionService).convertToResponseDtoReactively(eq(model), any());

        // call service operation
        Mono<ResponseDto> result = service.patch(modelDescriptor, patch, false);

        // launch reactive pipe & run assertions
        create(result)
                .assertNext(resultDto -> assertEquals(modelDescriptor, resultDto.getId()))
                .verifyComplete();

        // verify behaviour
        verify(modelRetriever).retrieveModelReactively(modelDescriptor);
        verify(patchFlow).patch(modelDescriptor, patch);
        verify(patcher).patch(modelDescriptor, model, patch);
        verify(dataSecurity).checkPatchAllowed(model);
        verify(modelSaver).saveModel(model);
        verify(dynamicModelConversionService).convertToResponseDtoReactively(eq(model), any());
    }

    @Test
    void remove() {
        ModelRetriever modelRetriever = mock(ModelRetriever.class);
        ReactiveDataSecurity dataSecurity = mock(ReactiveDataSecurity.class);
        DefaultDynamicModelReactiveRemover remover = mock(DefaultDynamicModelReactiveRemover.class);

        DynamicModelReactiveEverythingManagementService service = new DynamicModelReactiveEverythingManagementService(
                modelRetriever,
                null,
                dataSecurity,
                null,
                null,
                remover
        );

        Model model = mock(Model.class);

        // configure behaviour
        doReturn(just(model))
                .when(modelRetriever).retrieveModelReactively(modelDescriptor);
        doReturn(empty())
                .when(dataSecurity).checkRemovalAllowed(model);
        doReturn(empty())
                .when(remover).remove(modelDescriptor.getInternalId());

        // call service operation
        Mono<Void> result = service.remove(modelDescriptor);

        // launch a pipe & validate result
        create(result).verifyComplete();

        // verify behaviour
        verify(modelRetriever).retrieveModelReactively(modelDescriptor);
        verify(dataSecurity).checkRemovalAllowed(model);
        verify(remover).remove(modelDescriptor.getInternalId());
    }

    @RequiredArgsConstructor
    class ResponseDtoTest implements ResponseDto {
        private final Descriptor modelDescriptor;

        @Override
        public Descriptor getId() {
            return modelDescriptor;
        }

        @Override
        public Long getVersion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getCreated() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getModified() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getModel() {
            throw new UnsupportedOperationException();
        }
    }

    ;
}