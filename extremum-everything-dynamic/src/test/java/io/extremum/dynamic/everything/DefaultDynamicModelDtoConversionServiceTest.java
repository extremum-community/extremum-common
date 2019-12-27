package io.extremum.dynamic.everything;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class DefaultDynamicModelDtoConversionServiceTest {
    @Test
    void convertToResponseDtoReactively() throws IOException {
        DefaultDynamicModelDtoConversionService service = new DefaultDynamicModelDtoConversionService();

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .externalId("external-id")
                .modelType("DynModel_A")
                .build();

        JsonNode data = new ObjectMapper()
                .readValue("{\"a\":\"b\"}", JsonNode.class);

        JsonDynamicModel model = new JsonDynamicModel(descriptor, "DynModel_A", data);
        Mono<ResponseDto> result = service.convertToResponseDtoReactively(model, ConversionConfig.defaults());

        StepVerifier.create(result)
                .assertNext(dto -> {
                    Assertions.assertTrue(dto instanceof JsonDynamicModelResponseDto);

                    Assertions.assertEquals(model.getId(), dto.getId());
                    Assertions.assertEquals(model.getModelData(), ((JsonDynamicModelResponseDto) dto).getData());
                    Assertions.assertEquals(model.getModelName(), dto.getModel());
                })
                .verifyComplete();
    }
}
