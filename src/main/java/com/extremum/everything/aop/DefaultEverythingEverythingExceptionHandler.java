package com.extremum.everything.aop;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.response.Alert;
import com.extremum.common.response.Response;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static com.extremum.common.response.Alert.errorAlert;
import static com.extremum.common.response.Response.fail;


@RestControllerAdvice
@Slf4j
public class DefaultEverythingEverythingExceptionHandler implements EverythingEverythingExceptionHandler {

    @ExceptionHandler
    public @ResponseBody
    Response handleRequestDtoValidationException(RequestDtoValidationException e) {
        Set<ConstraintViolation<RequestDto>> constraintsViolation = e.getConatraintsViolation();

        log.error("{} will be occurs when validate a request DTO {}. Constraints violation: {}. The exception was catch in the DefaultEverythingEverythingExceptionHandler",
                e.getClass().getName(), e.getObject(), constraintsViolation);

        Response.Builder responseBuilder = Response.builder();

        for (ConstraintViolation<RequestDto> violation : constraintsViolation) {
            Alert alert = Alert.builder()
                    .withErrorLevel()
                    .withCode("400")
                    .withMessage(violation.getMessage() + ", you value: " + violation.getInvalidValue())
                    .withElement(violation.getPropertyPath().toString())
                    .build();

            responseBuilder.withAlert(alert);
        }

        return responseBuilder
                .withFailStatus()
                .withNowTimestamp()
                .withResult("Unable to complete 'everything-everything' operation")
                .build();
    }

    @ExceptionHandler
    public @ResponseBody
    Response handleEverythingEverythingException(EverythingEverythingException e) {
        log.debug("Exception was occurs and will be handled in EverythingEverythingExceptionHandler: {}",
                e.getLocalizedMessage(), e);

        return fail(errorAlert(e.getMessage()));
    }
}
