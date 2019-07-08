package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.Status;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.extremum.sharedmodels.dto.RequestDto;
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
    private String type;
    private Status status;
    private String slug;
    private String uri;
    private StringOrMultilingual name;
    private StringOrMultilingual description;
    private ComplexAddress address;
    @Valid
    private Coordinates coordinates;
    @Valid
    private List<Coordinates> boundary;
}
