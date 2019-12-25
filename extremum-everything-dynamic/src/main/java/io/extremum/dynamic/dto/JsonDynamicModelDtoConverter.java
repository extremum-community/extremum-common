package io.extremum.dynamic.dto;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.ToResponseDtoConverter;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import org.springframework.stereotype.Service;

@Service
public class JsonDynamicModelDtoConverter implements ToResponseDtoConverter<JsonDynamicModel, JsonDynamicModelResponseDto> {
    @Override
    public JsonDynamicModelResponseDto convertToResponse(JsonDynamicModel model, ConversionConfig config) {
        JsonDynamicModelResponseDto dto = new JsonDynamicModelResponseDto(model.getModelName());

        dto.setData(model.getModelData());

        return dto;
    }

    @Override
    public Class<? extends JsonDynamicModelResponseDto> getResponseDtoType() {
        return JsonDynamicModelResponseDto.class;
    }

    @Override
    public String getSupportedModel() {
        throw new UnsupportedOperationException("getSupportedModel() is not supported for dynamic model converter");
    }
}
