package com.extremum.sharedmodels.signal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;
import java.util.Set;

@DocumentationName("Signal")
public class Signal {
    public static final String MODEL_NAME = "Signal";

    @JsonProperty("sent")
    public ZonedDateTime sent = ZonedDateTime.now();

    @JsonProperty("locale")
    public String locale;

    @JsonProperty("type")
    public String type;

    @JsonProperty("initiated")
    public ZonedDateTime initiated;

    @JsonProperty("effective")
    public ZonedDateTime effective;

    @JsonPropertyOrder(alphabetic = true)
    @JsonProperty("clue")
    public Object clue;

    @JsonPropertyOrder(alphabetic = true)
    @JsonProperty("data")
    public Object data;

    public Set<String> personages;

    public enum FIELDS {
        sent, locale, type, initiated, clue, data, personages
    }
}
