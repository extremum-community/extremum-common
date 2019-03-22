package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.models.Model;

import java.util.function.Supplier;

public interface DtoConversionService {
    DtoConverter determineConverter(Model model);

    DtoConverter determineConverterOrElseThrow(Model model, Supplier<? extends RuntimeException> exceptionSupplier);

    ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config);

    RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config);
}
