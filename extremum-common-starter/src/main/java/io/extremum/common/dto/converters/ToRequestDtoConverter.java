package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.common.models.Model;

public interface ToRequestDtoConverter<M extends Model, D extends RequestDto> extends DtoConverter {
    D convertToRequest(M model, ConversionConfig config);

    Class<? extends D> getRequestDtoType();
}
