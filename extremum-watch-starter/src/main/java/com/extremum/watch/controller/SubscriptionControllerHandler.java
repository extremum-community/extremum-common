package com.extremum.watch.controller;

import com.extremum.common.response.Alert;
import com.extremum.common.response.Response;
import com.extremum.watch.exception.WatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("com.extremum.watch.controller")
public class SubscriptionControllerHandler {
    @ExceptionHandler(WatchException.class)
    public Response handleWatchException(WatchException e) {
        log.warn("Handle on {}: ", this.getClass().getSimpleName(), e);
        return Response.fail(Alert.errorAlert("You are unauthorized!"), HttpStatus.UNAUTHORIZED.value());
    }
}
