package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.models.Model;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;

import java.util.Optional;
import java.util.function.Supplier;

public interface DtoConversionService {
    DtoConverter findConverter(Class<? extends Model> modelClass);

    <M extends Model, D extends RequestDto>
    Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends RequestDto>
    Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(Class<? extends M> modelClass);

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);
}
