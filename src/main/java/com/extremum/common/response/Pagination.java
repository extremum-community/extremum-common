package com.extremum.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class Pagination {
    @JsonProperty
    private int offset;
    @JsonProperty
    private int count;
    @JsonProperty
    private int total;
    @JsonProperty
    private ZonedDateTime since;
    @JsonProperty
    private ZonedDateTime until;

    static Pagination singlePage(int total) {
        return Pagination.builder()
                .count(total)
                .total(total)
                .build();
    }
}
