package io.extremum.watch.controller;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.watch.exception.WatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("io.extremum.watch.controller")
@ConditionalOnBean(WatchController.class)
public class WatchControllersExceptionHandler {
    @ExceptionHandler
    public Response handleWatchException(WatchException e) {
        log.error("Exception has occurred and will be handled in WatchControllersExceptionHandler: {}",
                e.getLocalizedMessage(), e);
        return Response.fail(Alert.errorAlert("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler
    public Response handleEverythingAccessDeniedException(ExtremumAccessDeniedException e) {
        log.debug("Exception has occurred and will be handled in WatchControllersExceptionHandler: {}",
                e.getLocalizedMessage(), e);

        return Response.builder()
                .withFailStatus(HttpStatus.FORBIDDEN.value())
                .withNowTimestamp()
                .build();
    }
}
