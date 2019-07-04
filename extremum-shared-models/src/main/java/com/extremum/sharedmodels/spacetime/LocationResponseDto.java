package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.MultilingualObject;
import com.extremum.sharedmodels.basic.Status;
import com.extremum.sharedmodels.fundamental.CommonResponseDto;

import java.util.List;

@DocumentationName("Location")
public class LocationResponseDto extends CommonResponseDto {
    public String type;
    public Status status;
    public String slug;
    public String uri;
    public MultilingualObject name;
    public MultilingualObject description;
    public ComplexAddress address;
    public Coordinates coordinates;
    public List<Coordinates> boundary;
}
