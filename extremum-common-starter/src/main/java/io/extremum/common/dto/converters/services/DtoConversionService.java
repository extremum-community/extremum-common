package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.model.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface DtoConversionService {

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    Mono<ResponseDto> convertUnknownToResponseDtoReactively(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);

    <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<? extends Model> modelClass, D dto);

    Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass);
}
