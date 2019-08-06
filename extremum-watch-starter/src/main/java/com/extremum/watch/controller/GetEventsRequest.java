package com.extremum.watch.controller;

import com.extremum.common.utils.DateUtils;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author rpuch
 */
@ToString
public class GetEventsRequest {
    private final ZonedDateTime since;
    private final ZonedDateTime until;
    private final Integer limit;

    @ConstructorProperties({"since", "until", "limit"})
    public GetEventsRequest(
            @DateTimeFormat(pattern = DateUtils.FORMAT) ZonedDateTime since,
            @DateTimeFormat(pattern = DateUtils.FORMAT) ZonedDateTime until,
            Integer limit) {
        this.since = since;
        this.until = until;
        this.limit = limit;
    }

    public Optional<ZonedDateTime> getSince() {
        return Optional.ofNullable(since);
    }

    public Optional<ZonedDateTime> getUntil() {
        return Optional.ofNullable(until);
    }

    public OptionalInt getLimit() {
        if (limit == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(limit);
    }
}
