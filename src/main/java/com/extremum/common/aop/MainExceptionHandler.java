package com.extremum.common.aop;

import com.extremum.common.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.extremum.common.response.Alert.errorAlert;
import static com.extremum.common.response.Response.fail;

@Getter
@Setter
@RestControllerAdvice
public class MainExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainExceptionHandler.class);

    private boolean hideExceptionMessage = true;

    @ExceptionHandler(Exception.class)
    public @ResponseBody
    Response commonExceptionHandler(Exception e) {
        LOGGER.debug("Exception was occurred and will be handled of MainExceptionHandler: {}", e.getMessage(), e);

        if (hideExceptionMessage) {
            return fail(errorAlert("Errors was occurs while processing a request"));
        } else {
            return fail(errorAlert(e.getMessage()));
        }
    }
}
