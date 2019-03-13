package com.extremum.common.dto.converters;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.models.Model;

public interface ToRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    D convertToRequest(M model, ConversionConfig config);

    Class<? extends D> getRequestDtoType();
}
