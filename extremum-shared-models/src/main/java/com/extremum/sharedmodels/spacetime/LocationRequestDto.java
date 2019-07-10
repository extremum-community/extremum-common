package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.fundamental.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

@Getter
@Setter
@ToString
@DocumentationName("Location")
public class LocationRequestDto implements RequestDto {
    private Resource resource;
    private ComplexAddress address;
    @Valid
    private Coordinates coordinates;
    @Valid
    private List<Coordinates> boundary;
}
