package io.extremum.common.logging;

import io.extremum.sharedmodels.logging.LoggingConstants;
import io.extremum.sharedmodels.dto.Response;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Aspect
public class ReactiveRequestIdControllerAspect {
    @Around("isController() && returnsMono()")
    public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
        Object invocationResult = point.proceed();

        if (invocationResult instanceof Mono) {
            return monoWithRequestId((Mono<?>) invocationResult);
        }

        return invocationResult;
    }

    private Object monoWithRequestId(Mono<?> mono) {
        return mono.flatMap(this::applyRequestIdOnObjectIfItIsResponse)
                .subscriberContext(loggingContext());
    }

    private Mono<?> applyRequestIdOnObjectIfItIsResponse(Object object) {
        if (object instanceof Response) {
            Response response = (Response) object;
            return Mono.subscriberContext()
                    .map(context -> context.getOrDefault(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, "<none>"))
                    .map(response::withRequestId);
        }
        return Mono.just(object);
    }

    private Context loggingContext() {
        return Context.of(LoggingConstants.REQUEST_ID_ATTRIBUTE_NAME, randomRequestId());
    }

    private String randomRequestId() {
        return UUID.randomUUID().toString();
    }

    @Pointcut("" +
            "within(@org.springframework.stereotype.Controller *) || " +
            "within(@(@org.springframework.stereotype.Controller *) *)")
    private void isController() {
    }

    @Pointcut("execution(reactor.core.publisher.Mono *..*.*(..))")
    private void returnsMono() {
    }
}
