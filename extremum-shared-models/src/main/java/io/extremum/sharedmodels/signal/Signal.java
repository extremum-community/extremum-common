package io.extremum.sharedmodels.signal;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.annotation.DocumentationName;
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

    private Descriptor id;

    @JsonProperty("sent")
    private ZonedDateTime sent = ZonedDateTime.now();

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
        id, sent, type, initiated, effective, clue, data
    }
}
