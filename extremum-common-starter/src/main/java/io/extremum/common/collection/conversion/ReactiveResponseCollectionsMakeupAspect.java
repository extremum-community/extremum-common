package io.extremum.common.collection.conversion;

import io.extremum.common.response.advice.ReactiveResponseDtoHandlingAspect;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class ReactiveResponseCollectionsMakeupAspect extends ReactiveResponseDtoHandlingAspect {
    private final CollectionMakeup makeup;

    @Override
    protected Mono<?> applyToResponseDto(ResponseDto responseDto) {
        return makeup.applyCollectionMakeupReactively(responseDto);
    }
}