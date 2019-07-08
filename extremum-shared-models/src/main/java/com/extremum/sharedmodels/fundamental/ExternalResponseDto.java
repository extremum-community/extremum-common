package com.extremum.sharedmodels.fundamental;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@DocumentationName("External")
public class ExternalResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "External";

    private String clue;
    private String system;
    private String qualifier;
    private Set<Descriptor> objects;

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
