package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.basic.IdOrObjectStruct;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.extremum.sharedmodels.spacetime.LocationRequestDto;
import com.extremum.sharedmodels.spacetime.TimeFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author rpuch
 */
@Getter
@Setter
@ToString
public class PersonPositionForRequestDto {
    private StringOrMultilingual company;
    private StringOrMultilingual title;
    private StringOrMultilingual description;
    private TimeFrame timeframe;
    private IdOrObjectStruct<String, LocationRequestDto> location;
}
