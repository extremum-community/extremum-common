package com.extremum.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Common response structure
 */
@Getter
public class Response {
    private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);
    public static final int OK_HTTP_STATUS_CODE = 200;

    private ResponseStatusEnum status;
    private Integer code;
    private ZonedDateTime timestamp;
    @JsonProperty("rqid")
    private String requestId;
    private String locale;
    private List<Alert> alerts;
    private Object result;

    public static Builder builder() {
        return new Builder();
    }

    public static Response ok(Object result) {
        return builder()
                .withOkStatus()
                .withResult(result)
                .withNowTimestamp()
                .build();
    }

    public static Response ok() {
        return builder().withOkStatus().build();
    }

    public static Response fail() {
        return builder()
                .withFailStatus()
                .withNowTimestamp()
                .build();
    }

    public static Response fail(Alert alert) {
        return Response.builder()
                .withFailStatus()
                .withAlert(alert)
                .withNowTimestamp()
                .build();
    }

    private static class Builder {
        private ResponseStatusEnum status;
        private Integer code = OK_HTTP_STATUS_CODE;
        private Object result;
        private String locale;
        private List<Alert> alerts;
        private ZonedDateTime timestamp;

        public Builder withOkStatus() {
            this.status = ResponseStatusEnum.OK;
            this.code = OK_HTTP_STATUS_CODE;

            withNowTimestamp();

            return this;
        }

        public Builder withFailStatus() {
            this.status = ResponseStatusEnum.FAIL;
            this.code = OK_HTTP_STATUS_CODE;

            withNowTimestamp();

            return this;
        }

        public Builder withResult(Object result) {
            this.result = result;
            return this;
        }

        public Builder withAlert(Alert alert) {
            withAlerts(Collections.singletonList(alert));

            return this;
        }

        public Builder withAlerts(Collection<Alert> alerts) {
            if (this.alerts == null) {
                this.alerts = new ArrayList<>();
            }

            this.alerts.addAll(alerts);

            return this;
        }

        public Builder withNowTimestamp() {
            timestamp = ZonedDateTime.now();
            return this;
        }

        public Response build() {
            requireNonNull(status, "Status can't be null");
            requireNonNull(code, "Code can't be null");

            Response response = new Response();

            response.status = status;
            response.code = code;
            response.result = result;
            response.alerts = alerts;
            response.requestId = tryToDetermineRequestId();
            response.timestamp = timestamp;
            response.locale = (this.locale == null ? Constants.DEFAULT_LOCALE : this.locale);

            return response;
        }

        private String tryToDetermineRequestId() {
            return MDC.get(Constants.REQUEST_ID_ATTRIBUTE_NAME);
        }
    }
}
