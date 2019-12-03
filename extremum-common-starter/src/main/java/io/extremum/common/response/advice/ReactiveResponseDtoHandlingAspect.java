package io.extremum.common.response.advice;

import io.extremum.common.utils.StreamUtils;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
@Aspect
public abstract class ReactiveResponseDtoHandlingAspect {
    protected abstract Mono<?> applyToResponseDto(ResponseDto responseDto);

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
        return mono.flatMap(this::possiblyApplyMakeupToResponseDtoInsideResponseOrSSE);
    }

    private Object applyMakeupToFluxPayload(Flux<?> flux) {
        return flux.concatMap(this::possiblyApplyMakeupToResponseDtoInsideResponseOrSSE);
    }

    private Mono<?> possiblyApplyMakeupToResponseDtoInsideResponseOrSSE(Object object) {
        if (object instanceof Response) {
            Response response = (Response) object;
            Object payload = response.getResult();
            if (payload instanceof ResponseDto) {
                return applyMakeupToResponseDtoThenReturnResponse((ResponseDto) payload, response);
            } else if (payload instanceof ResponseDto[]) {
                ResponseDto[] responseDtos = (ResponseDto[]) payload;
                return applyMakeupToResponseDtosInListAndReturnResponse(Arrays.asList(responseDtos), response);
            } else if (payload instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) payload;
                return applyMakeupToResponseDtosInIterableAndReturnResponse(iterable, response);
            }
        }
        if (object instanceof ServerSentEvent) {
            ServerSentEvent<?> sse = (ServerSentEvent<?>) object;
            if (sse.data() instanceof ResponseDto) {
                ResponseDto responseDto = (ResponseDto) sse.data();
                return applyMakeupToResponseDtoThenReturnResponse(responseDto, sse);
            }
        }

        return Mono.just(object);
    }

    private Mono<?> applyMakeupToResponseDtoThenReturnResponse(ResponseDto responseDto, Object response) {
        return applyToResponseDto(responseDto)
                .thenReturn(response);
    }

    private Mono<?> applyMakeupToResponseDtosInIterableAndReturnResponse(Iterable<?> iterable, Response response) {
        List<ResponseDto> responseDtos = StreamUtils.fromIterable(iterable)
                .filter(obj -> obj instanceof ResponseDto)
                .map(ResponseDto.class::cast)
                .collect(Collectors.toList());
        return applyMakeupToResponseDtosInListAndReturnResponse(responseDtos, response);
    }

    private Mono<?> applyMakeupToResponseDtosInListAndReturnResponse(List<ResponseDto> responseDtos,
                                                                     Response response) {
        return Flux.fromIterable(responseDtos)
                .concatMap(this::applyToResponseDto)
                .then(Mono.just(response));
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