package com.extremum.sharedmodels.spacetime;

import com.extremum.common.dto.RequestDto;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.MultilingualObject;
import com.extremum.sharedmodels.basic.Status;

import javax.validation.Valid;
import java.util.List;

@DocumentationName("Location")
public class LocationRequestDto implements RequestDto {
    public String type;
    public Status status;
    public String slug;
    public String uri;
    public MultilingualObject name;
    public MultilingualObject description;
    public ComplexAddress address;
    @Valid
    public Coordinates coordinates;
    @Valid
    public List<Coordinates> boundary;
}
