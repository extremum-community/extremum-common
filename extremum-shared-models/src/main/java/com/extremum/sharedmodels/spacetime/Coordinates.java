package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by vov4a on 07.09.17.
 */
@DocumentationName("Coordinates")
public class Coordinates {

    @NotNull
    @JsonProperty("latitude")
    public Double latitude;

    @NotNull
    @JsonProperty("longitude")
    public Double longitude;

    public enum FIELDS {
        latitude, longitude
    }
}
