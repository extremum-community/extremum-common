package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.models.Model;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;

import java.util.Optional;
import java.util.function.Supplier;

public interface DtoConversionService {
    DtoConverter findConverter(Class<? extends Model> modelClass);

    DtoConverter findConverterOrThrow(Model model, Supplier<? extends RuntimeException> exceptionSupplier);

    <M extends Model, D extends RequestDto>
    Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(Class<? extends M> modelClass);

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);
}
