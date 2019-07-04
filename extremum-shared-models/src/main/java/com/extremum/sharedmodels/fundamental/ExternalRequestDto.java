package com.extremum.sharedmodels.fundamental;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.RequestDto;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.annotation.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@DocumentationName("External")
public class ExternalRequestDto implements RequestDto {
    @NotNull
    @NotEmpty
    public String clue;

    @NotNull
    @NotEmpty
    public String system;

    @NotNull
    @NotEmpty
    public String qualifier;

    @NotNull
    @Size(min = 1)
    public Set<Descriptor> objects;
}
