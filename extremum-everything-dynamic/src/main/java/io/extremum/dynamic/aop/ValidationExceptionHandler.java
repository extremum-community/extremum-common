package io.extremum.dynamic.aop;

import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(DynamicModelValidationException.class)
    public Mono<Response> handleDynamicModelValidationException(DynamicModelValidationException e) {
        List<Alert> alerts = new ArrayList<>();

        e.getViolations().stream()
                .map(Violation::getMessage)
                .map(Alert::errorAlert)
                .forEach(alerts::add);

        return Mono.just(Response.fail(alerts, HttpStatus.BAD_REQUEST.value()));
    }
}
