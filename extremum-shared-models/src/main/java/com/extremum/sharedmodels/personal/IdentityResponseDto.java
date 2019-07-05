package com.extremum.sharedmodels.personal;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.stucts.IdOrObjectStruct;
import com.extremum.sharedmodels.annotation.DocumentationName;
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
