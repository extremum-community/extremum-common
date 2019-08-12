package com.extremum.watch.end2end.fixture;

import com.extremum.sharedmodels.dto.RequestDto;
import lombok.Data;

/**
 * @author rpuch
 */
@Data
public class WatchedModelRequestDto implements RequestDto {
    private String name;
}
