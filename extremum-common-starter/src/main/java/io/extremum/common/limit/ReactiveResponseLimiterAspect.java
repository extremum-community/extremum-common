package io.extremum.common.limit;

import io.extremum.common.response.advice.ReactiveResponseDtoHandlingAspect;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class ReactiveResponseLimiterAspect extends ReactiveResponseDtoHandlingAspect {
    private final ResponseLimiter limiter;

    @Override
    protected Mono<?> applyToResponseDto(ResponseDto responseDto) {
        return Mono.fromCallable(() -> {
            limiter.limit(responseDto);
            return Mono.empty();
        });
    }
}
