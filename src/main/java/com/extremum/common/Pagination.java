package com.extremum.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class Pagination {
    private int offset;
    private int count;
    private int total;
    private ZonedDateTime since;
    private ZonedDateTime until;

    static Pagination singlePage(int total) {
        return Pagination.builder()
                .count(total)
                .total(total)
                .build();
    }
}
