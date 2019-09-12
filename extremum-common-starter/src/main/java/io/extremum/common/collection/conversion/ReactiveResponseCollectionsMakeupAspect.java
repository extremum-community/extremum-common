package io.extremum.common.collection.conversion;

import io.extremum.common.response.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@Aspect
@RequiredArgsConstructor
public class ReactiveResponseCollectionsMakeupAspect {
    private final CollectionMakeup makeup;

    @Around("isController() && (returnsMono() || returnsFlux())")
    public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
        Object invocationResult = point.proceed();

        if (invocationResult instanceof Mono) {
            return applyMakeupToMonoPayload((Mono<?>) invocationResult);
        }
        if (invocationResult instanceof Flux) {
            return applyMakeupToFluxPayload((Flux<?>) invocationResult);
        }

        return invocationResult;
    }

    private Object applyMakeupToMonoPayload(Mono<?> mono) {
        return mono.flatMap(this::possiblyApplyMakeupToResponseDtoInsideResponse);
    }

    private Object applyMakeupToFluxPayload(Flux<?> flux) {
        return flux.flatMap(this::possiblyApplyMakeupToResponseDtoInsideResponse);
    }

    private Mono<?> possiblyApplyMakeupToResponseDtoInsideResponse(Object object) {
        if (object instanceof Response) {
            Response response = (Response) object;
            if (response.getResult() instanceof ResponseDto) {
                ResponseDto responseDto = (ResponseDto) response.getResult();
                return makeup.applyCollectionMakeupReactively(responseDto)
                        .thenReturn(response);
            }
        }

        return Mono.just(object);
    }

    /**
     * This means 'method calls on instances of classes annotated with @Controller
     * directly or via a meta-annotated annotation (with one level of indirection at max).
     */
    @Pointcut("" +
            "within(@org.springframework.stereotype.Controller *) || " +
            "within(@(@org.springframework.stereotype.Controller *) *)")
    private void isController() {
    }

    @Pointcut("execution(reactor.core.publisher.Mono *..*.*(..))")
    private void returnsMono() {
    }

    @Pointcut("execution(reactor.core.publisher.Flux *..*.*(..))")
    private void returnsFlux() {
    }
}