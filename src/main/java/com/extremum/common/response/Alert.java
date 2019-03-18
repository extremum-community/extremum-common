package com.extremum.common.response;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class Alert {
    private AlertLevelEnum level;
    private String code;
    private ZonedDateTime timestamp;
    private String element;
    private String message;
    private String link;
    private String traceId;

    public static Alert errorAlert(String errorMessage) {
        return Alert.builder()
                .withErrorLevel()
                .withMessage(errorMessage)
                .withNowTimestamp()
                .build();
    }

    public boolean isError() {
        return this.level == AlertLevelEnum.ERROR;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertLevelEnum level;
        private String message;
        private ZonedDateTime timestamp;
        private String element;
        private String code;

        public Builder withErrorLevel() {
            level = AlertLevelEnum.ERROR;
            return this;
        }

        public Builder withMessage(String errorMessage) {
            message = errorMessage;
            return this;
        }

        public Builder withNowTimestamp() {
            timestamp = ZonedDateTime.now();
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withElement(String element) {
            this.element = element;
            return this;
        }

        public Alert build() {
            Alert alert = new Alert();

            alert.level = level;
            alert.message = message;
            alert.timestamp = timestamp;
            alert.code = code;
            alert.element = element;

            return alert;
        }
    }
}
