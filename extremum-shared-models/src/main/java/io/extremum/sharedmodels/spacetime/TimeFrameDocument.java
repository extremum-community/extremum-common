package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.IntegerOrString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

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
    private int duration;

    public enum FIELDS {
        start, end, duration
    }
}
