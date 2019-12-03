package io.extremum.common.descriptor.resolve;

import io.extremum.common.response.advice.ReactiveResponseDtoHandlingAspect;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@Aspect
@RequiredArgsConstructor
public class ReactiveDescriptorResolvingAspect extends ReactiveResponseDtoHandlingAspect {
    private final ResponseDtoDescriptorResolver descriptorResolver;

    @Override
    protected Mono<?> applyToResponseDto(ResponseDto responseDto) {
        return descriptorResolver.resolveExternalIdsIn(responseDto);
    }
}
