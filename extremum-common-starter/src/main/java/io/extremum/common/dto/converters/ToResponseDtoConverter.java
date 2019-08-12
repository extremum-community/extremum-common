package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.common.models.Model;

public interface ToResponseDtoConverter<M extends Model, D extends ResponseDto> extends DtoConverter {
    D convertToResponse(M model, ConversionConfig config);

    Class<? extends D> getResponseDtoType();
}
