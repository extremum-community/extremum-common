package com.extremum.watch.controller;

import com.extremum.common.utils.DateUtils;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@Getter
@ToString
public class GetEventsRequest {
    private final ZonedDateTime since;
    private final ZonedDateTime until;

    @ConstructorProperties({"since", "until"})
    public GetEventsRequest(
            @DateTimeFormat(pattern = DateUtils.FORMAT) ZonedDateTime since,
            @DateTimeFormat(pattern = DateUtils.FORMAT) ZonedDateTime until) {
        this.since = since;
        this.until = until;
    }
}
