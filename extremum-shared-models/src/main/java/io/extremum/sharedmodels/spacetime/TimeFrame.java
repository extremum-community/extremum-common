package io.extremum.sharedmodels.spacetime;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.IntegerOrString;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by vov4a on 18.06.17.
 */
@Getter
@Setter
@RequiredArgsConstructor
@ToString
@DocumentationName("Timeframe")
public class TimeFrame {

    @JsonProperty("start")
    private ZonedDateTime start;

    @JsonProperty("end")
    private ZonedDateTime end;

    @JsonProperty("duration")
    private IntegerOrString duration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFrame timeFrame = (TimeFrame) o;
        return Objects.equals(start, timeFrame.start) &&
                Objects.equals(end, timeFrame.end) &&
                Objects.equals(javaDuration(), timeFrame.javaDuration());
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, javaDuration());
    }

    public Duration javaDuration() {
        if (duration == null) {
            return null;
        } else if (duration.isInteger()) {
            return Duration.ofMillis(duration.getIntegerValue());
        } else /* (duration.isString()) */ {
            return parseDuration(duration.getStringValue());
        }
    }

    public enum FIELDS {
        start, end, duration
    }

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\W*(ms|us|ns|d|h|m|s)");
    static Duration parseDuration(String duration) {
        Duration r = Duration.ZERO;
        Matcher m = DURATION_PATTERN.matcher(duration);
        while (m.find()) {
            int count = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            switch (unit) {
                case "d":
                    r = r.plusDays(count);
                    break;
                case "h":
                    r = r.plusHours(count);
                    break;
                case "m":
                    r = r.plusMinutes(count);
                    break;
                case "s":
                    r = r.plusSeconds(count);
                    break;
                case "ms":
                    r = r.plusMillis(count);
                    break;
                case "us":
                case "ns":
                    // ignore for now
            }
        }
        return r;
    }

}
