package com.extremum.sharedmodels.fundamental;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.sharedmodels.annotation.DocumentationName;

import java.util.Set;

@DocumentationName("External")
public class ExternalResponseDto extends CommonResponseDto {
    public String clue;
    public String system;
    public String qualifier;
    public Set<Descriptor> objects;
}
