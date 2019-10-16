package io.extremum.sharedmodels.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class Projection {
    public Integer offset;
    public Integer limit;
    public ZonedDateTime since;
    public ZonedDateTime until;
}
