package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.models.Model;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;

public interface DtoConversionService {

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);

    <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<? extends Model> modelClass, D dto);

    Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass);
}
