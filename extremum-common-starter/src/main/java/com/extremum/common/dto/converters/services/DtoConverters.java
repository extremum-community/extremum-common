package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.ToResponseDtoConverter;
import com.extremum.common.models.Model;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface DtoConverters {
    <M extends Model, D extends RequestDto>
    Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends RequestDto>
    Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(Class<? extends M> modelClass);

    <M extends Model, D extends ResponseDto>
    Optional<ToResponseDtoConverter<M, D>> findToResponseDtoConverter(Class<? extends M> modelClass);
}
