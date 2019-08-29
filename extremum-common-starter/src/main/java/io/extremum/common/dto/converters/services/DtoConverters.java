package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToResponseDtoConverter;
import io.extremum.common.dto.converters.ToRequestDtoConverter;
import io.extremum.common.dto.converters.ToResponseDtoConverter;
import io.extremum.common.models.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;

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

    <M extends Model, D extends ResponseDto>
    Optional<ReactiveToResponseDtoConverter<M, D>> findReactiveToResponseDtoConverter(Class<? extends M> modelClass);
}
