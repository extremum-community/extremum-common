package com.extremum.common.dto.converters;

import com.extremum.common.dto.ResponseDto;
import com.extremum.common.models.Model;

public interface ToResponseDtoConverter<M extends Model, D extends ResponseDto> extends DtoConverter {
    D convertToResponse(M model, ConversionConfig config);

    Class<? extends D> getResponseDtoType();
}
