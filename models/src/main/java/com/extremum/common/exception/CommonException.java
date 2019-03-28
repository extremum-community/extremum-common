package com.extremum.common.exception;


import com.extremum.common.response.Alert;
import com.extremum.common.response.Response;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author vov4a, scalagrammer
 */
public class CommonException extends RuntimeException {

    private final List<Alert> alerts = new ArrayList<>();

    private final int code;

    public CommonException(String message, HttpStatus status) {
        super(message);
        this.code = status.value();
        this.alerts.add(Alert.errorAlert(message, null, status.toString()));
    }

    public CommonException(Throwable cause, String message, HttpStatus status) {
        super(message, cause);
        this.code = status.value();
        this.alerts.add(Alert.errorAlert(message, null, status.toString()));
    }

    public CommonException(int code) {
        this.code = code;
    }

    public CommonException withAlerts(Collection<Alert> alerts) {
        this.alerts.addAll(alerts);
        return this;
    }

    public CommonException withAlert(Alert alert) {
        this.alerts.add(alert);
        return this;
    }

    public int getCode() {
        return code;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    private Response toFailResponse(String locale) {
        return Response.builder()
                .withFailStatus()
                .withLocale(locale)
                .withCode(code)
                .withAlerts(alerts)
                .build();
    }

    private Response toFailResponse() {
        return this.toFailResponse(null); // without locale
    }

    @Override
    public String toString() {
        return toFailResponse().toString();
    }
}
