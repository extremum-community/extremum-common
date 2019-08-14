package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.models.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;

public interface DtoConversionService {

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);

    <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<? extends Model> modelClass, D dto);

    Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass);
}