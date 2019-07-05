package com.extremum.sharedmodels.spacetime;

import com.extremum.common.stucts.IntegerOrString;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;


/**
 * Created by vov4a on 18.06.17.
 */
@Data
@DocumentationName("Timeframe")
public class TimeFrame {

    @JsonProperty("start")
    private ZonedDateTime start;

    @JsonProperty("end")
    private ZonedDateTime end;

    @JsonProperty("duration")
    private IntegerOrString duration;

    public enum FIELDS {
        start, end, duration
    }
}
