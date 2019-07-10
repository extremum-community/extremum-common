package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.basic.IdOrObject;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.extremum.sharedmodels.spacetime.LocationResponseDto;
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
public class PersonPositionForResponseDto {
    private StringOrMultilingual company;
    private StringOrMultilingual title;
    private StringOrMultilingual description;
    private TimeFrame timeframe;
    private IdOrObject<String, LocationResponseDto> location;
}
