package io.extremum.everything.aop;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.exceptions.RequestDtoValidationException;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static io.extremum.sharedmodels.dto.Alert.errorAlert;
import static io.extremum.sharedmodels.dto.Response.fail;


@RestControllerAdvice(annotations = EverythingExceptionHandlerTarget.class)
@Slf4j
public class DefaultEverythingEverythingExceptionHandler implements EverythingEverythingExceptionHandler {

    @ExceptionHandler
    public Response handleRequestDtoValidationException(RequestDtoValidationException e) {
        Set<ConstraintViolation<RequestDto>> constraintsViolation = e.getConatraintsViolation();

        log.error("{} has occurred while validating a request DTO {}. Constraints violation: {}. " +
                        "The exception was caught in the DefaultEverythingEverythingExceptionHandler",
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
                .withFailStatus(HttpStatus.BAD_REQUEST.value())
                .withNowTimestamp()
                .withResult("Unable to complete 'everything-everything' operation")
                .build();
    }

    @ExceptionHandler
    public Response handleEverythingEverythingException(EverythingEverythingException e) {
        logException(e);

        return fail(errorAlert(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private void logException(Exception e) {
        log.debug("Exception has occurred and will be handled in DefaultEverythingEverythingExceptionHandler: {}",
                e.getLocalizedMessage(), e);
    }

    @ExceptionHandler
    public Response handleModelNotFoundException(ModelNotFoundException e) {
        logException(e);

        return notFound();
    }

    private Response notFound() {
        return Response.builder()
                .withFailStatus(HttpStatus.NOT_FOUND.value())
                .withNowTimestamp()
                .build();
    }

    @ExceptionHandler
    public Response handleDescriptorNotFoundException(DescriptorNotFoundException e) {
        logException(e);

        return notFound();
    }

    @ExceptionHandler
    public Response handleDescriptorNotReadyException(DescriptorNotReadyException e) {
        logException(e);

        return Response.builder()
                .withDoingStatus()
                .withAlert(Alert.infoAlert("Requested entity is still being processed, please retry later"))
                .withNowTimestamp()
                .build();
    }

    @ExceptionHandler
    public Response handleEverythingAccessDeniedException(ExtremumAccessDeniedException e) {
        log.debug("Exception has occurred and will be handled in DefaultEverythingEverythingExceptionHandler: {}",
                e.getLocalizedMessage(), e);

        return Response.builder()
                .withFailStatus(HttpStatus.FORBIDDEN.value())
                .withNowTimestamp()
                .build();
    }
}
