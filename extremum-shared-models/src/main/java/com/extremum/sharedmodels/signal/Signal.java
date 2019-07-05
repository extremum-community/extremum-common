package com.extremum.sharedmodels.signal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@DocumentationName("Signal")
public class Signal {
    public static final String MODEL_NAME = "Signal";

    @JsonProperty("sent")
    private ZonedDateTime sent = ZonedDateTime.now();

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("type")
    private String type;

    @JsonProperty("initiated")
    private ZonedDateTime initiated;

    @JsonProperty("effective")
    private ZonedDateTime effective;

    @JsonPropertyOrder(alphabetic = true)
    @JsonProperty("clue")
    private Object clue;

    @JsonPropertyOrder(alphabetic = true)
    @JsonProperty("data")
    private Object data;

    public enum FIELDS {
        sent, locale, type, initiated, clue, data
    }
}
