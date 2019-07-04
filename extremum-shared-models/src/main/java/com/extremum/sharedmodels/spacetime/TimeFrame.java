package com.extremum.sharedmodels.spacetime;

import com.extremum.common.stucts.IntegerOrString;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;


/**
 * Created by vov4a on 18.06.17.
 */
@DocumentationName("Timeframe")
public class TimeFrame {

    @JsonProperty("start")
    public ZonedDateTime start;

    @JsonProperty("end")
    public ZonedDateTime end;

    @JsonProperty("duration")
    public IntegerOrString duration;

    public enum FIELDS {
        START("start"), END("end");

        private final String value;

        FIELDS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
