package com.extremum.common.response;

import com.extremum.common.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.OK;

/**
 * Common response structure
 */
@Getter
public class Response {
    private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);

    private ResponseStatusEnum status;
    private Integer code;
    private ZonedDateTime timestamp;
    @JsonProperty("rqid")
    private String requestId;
    private String locale;
    private List<Alert> alerts;
    private Object result;
    @JsonProperty("paged")
    private Pagination pagination;

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Response response) {
        return new Builder(response);
    }

    public static Response ok(Object result) {
        return ok(result, emptyList());
    }

    public static Response ok(Object result, Alert alert) {
        return ok(result, singletonList(alert));
    }

    public static Response ok(Object result, List<Alert> alerts) {
        return builder()
                .withOkStatus()
                .withAlerts(alerts)
                .withResult(result)
                .withNowTimestamp()
                .build();
    }

    public static Response ok(Collection<? extends Serializable> result, Pagination pagination) {
        return builder()
                .withOkStatus()
                .withResult(result, pagination)
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
        return fail(alert, 200);
    }

    public static Response fail(Alert alert, int code) {
        return fail(singletonList(alert), code);
    }

    public static Response fail(Collection<Alert> alerts) {
        return fail(alerts, 200);
    }

    public static Response fail(Collection<Alert> alerts, int code) {
        return Response.builder()
                .withFailStatus()
                .withCode(code)
                .withAlerts(alerts)
                .withNowTimestamp()
                .build();
    }

    public boolean hasAlerts() {
        return alerts != null && !alerts.isEmpty();
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", code=" + code +
                ", timestamp=" + timestamp +
                ", requestId='" + requestId + '\'' +
                ", locale='" + locale + '\'' +
                ", alerts=" + alerts +
                ", result=" + result +
                ", pagination=" + pagination +
                '}';
    }

    public static class Builder {
        private ResponseStatusEnum status;
        private Integer code = OK.value();
        private Object result;
        private String locale;
        private List<Alert> alerts;
        private ZonedDateTime timestamp;
        private Pagination pagination;
        private String requestId;

        public Builder() {
        }

        public Builder(Response response) {
            this.status = response.status;
            this.code = response.code;
            this.result = response.result;
            this.locale = response.locale;
            this.alerts = response.alerts;
            this.timestamp = response.timestamp;
            this.pagination = response.pagination;
            this.requestId = response.requestId;
        }

        public Builder withOkStatus() {
            this.status = ResponseStatusEnum.OK;
            this.code = OK.value();

            withNowTimestamp();

            return this;
        }

        public Builder withFailStatus() {
            this.status = ResponseStatusEnum.FAIL;
            this.code = OK.value();

            withNowTimestamp();

            return this;
        }

        public Builder withDoingStatus() {
            this.status = ResponseStatusEnum.DOING;
            this.code = OK.value();

            withNowTimestamp();

            return this;
        }

        public Builder withWarningStatus() {
            this.status = ResponseStatusEnum.WARNING;
            this.code = OK.value();

            withNowTimestamp();

            return this;
        }


        public Builder withPagination(Pagination pagination) {
            this.pagination = pagination;
            return this;
        }


        public Builder withResult(Object result) {
            this.result = result;
            if (pagination == null && result instanceof Collections) {
                pagination = Pagination.singlePage(((Collection) result).size());
            }
            return this;
        }

        public Builder withResult(Collection<? extends Serializable> result, Pagination pagination) {
            this.result = result;
            this.pagination = pagination;
            return this;
        }

        public Builder withAlert(Alert alert) {
            withAlerts(singletonList(alert));

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

        public Builder withLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder withCode(int code) {
            this.code = code;
            return this;
        }

        public Builder withRequestId(String requestId) {
            this.requestId = requestId;
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
            if (this.requestId == null) {
                response.requestId = tryToDetermineRequestId();
            } else {
                response.requestId = this.requestId;
            }
            response.timestamp = timestamp;
            response.locale = (this.locale == null ? Locale.getDefault().toLanguageTag() : this.locale);
            response.pagination = pagination;

            return response;
        }

        private String tryToDetermineRequestId() {
            return MDC.get(Constants.REQUEST_ID_ATTRIBUTE_NAME);
        }
    }
}
