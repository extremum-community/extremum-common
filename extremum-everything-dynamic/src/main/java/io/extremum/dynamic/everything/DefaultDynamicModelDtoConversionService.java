package io.extremum.dynamic.everything;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DefaultDynamicModelDtoConversionService implements DynamicModelDtoConversionService {
    @Override
    public Mono<ResponseDto> convertToResponseDtoReactively(Model model, ConversionConfig config) {
        if (model instanceof JsonDynamicModel) {
            return Mono.fromSupplier(() -> {
                JsonDynamicModel dModel = (JsonDynamicModel) model;

                JsonDynamicModelResponseDto dto = new JsonDynamicModelResponseDto();
                dto.setData(dModel.getModelData());
                dto.setId(dModel.getId());
                dto.setModel(dModel.getModelName());

                return dto;
            });
        } else {
            return Mono.error(new IllegalArgumentException("Only JsonDynamicModel supported"));
        }
    }
}
