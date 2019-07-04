package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Created by vov4a on 07.09.17.
 */
@DocumentationName("Position")
public class Position {

    @NotNull
    @JsonProperty("timestamp")
    public ZonedDateTime timestamp;

    @NotNull
    @JsonProperty("latitude")
    public Number latitude;

    @NotNull
    @JsonProperty("longitude")
    public Number longitude;

    @JsonProperty("accuracy")
    public Number accuracy;

    @JsonProperty("altitude")
    public Number altitude;

    @JsonProperty("altitudeAccuracy")
    public Number altitudeAccuracy;

    @JsonProperty("heading")
    public Number heading;

    @JsonProperty("speed")
    public Number speed;

    public enum FIELDS {
        timestamp, latitude, longitude, accuracy, altitude, altitudeAccuracy, heading, speed
    }
}
