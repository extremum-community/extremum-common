package io.extremum.common.dto.converters;

import io.extremum.common.models.Model;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

public interface ToResponseDtoConverter<M extends Model, D extends ResponseDto> extends DtoConverter {
    D convertToResponse(M model, ConversionConfig config);

    default Mono<D> convertToResponseReactively(M model, ConversionConfig config) {
        return Mono.defer(() -> Mono.just(convertToResponse(model, config)));
    }

    Class<? extends D> getResponseDtoType();
}
