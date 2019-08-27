package io.extremum.common.dto.converters;

import io.extremum.common.models.Model;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface ReactiveToResponseDtoConverter<M extends Model, D extends ResponseDto> extends DtoConverter {
    Mono<D> convertToResponseReactively(M model, ConversionConfig config);

    Class<? extends D> getResponseDtoType();
}
