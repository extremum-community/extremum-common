package io.extremum.sharedmodels.spacetime;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.IntegerOrString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents Timeframe for storage in a document-oriented database like Mongo.
 * This means that duration is also stored in milliseconds.
 */
@Getter
@Setter
@RequiredArgsConstructor
@ToString
@DocumentationName("Timeframe")
public class TimeFrameDocument {

    private ZonedDateTime start;
    private ZonedDateTime end;
    private IntegerOrString duration;
    private int durationMillis;

    public enum FIELDS {
        start, end, duration, durationMillis
    }
}
