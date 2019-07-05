package com.extremum.sharedmodels.personal;

import com.extremum.common.stucts.IdOrObjectStruct;
import com.extremum.common.stucts.MultilingualObject;
import com.extremum.sharedmodels.spacetime.LocationRequestDto;
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
public class PersonPositionForRequestDto {
    private MultilingualObject company;
    private MultilingualObject title;
    private MultilingualObject description;
    private TimeFrame timeframe;
    private IdOrObjectStruct<String, LocationRequestDto> location;
}
