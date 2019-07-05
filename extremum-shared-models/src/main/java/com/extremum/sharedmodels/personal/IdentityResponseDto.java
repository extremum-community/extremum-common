package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.IdOrObjectStruct;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.*;

import java.util.Locale;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@DocumentationName({"Agent", "Identity"})
public class IdentityResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "Identity";

    private boolean verified;
    private Locale locale;
    private String timezone;
    private IdOrObjectStruct<Descriptor, PersonResponseDto> person;

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
