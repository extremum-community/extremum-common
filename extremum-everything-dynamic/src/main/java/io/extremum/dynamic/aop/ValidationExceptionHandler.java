package io.extremum.dynamic.aop;

import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(DynamicModelValidationException.class)
    public Mono<Response> handleDynamicModelValidationException(DynamicModelValidationException e) {
        List<Alert> alerts = e.getViolations().stream()
                .map(Violation::getMessage)
                .map(Alert::errorAlert)
                .collect(toList());

        return Mono.just(Response.fail(alerts, HttpStatus.BAD_REQUEST.value()));
    }
}
