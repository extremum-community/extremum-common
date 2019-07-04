package com.extremum.sharedmodels.personal;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.RequestDto;
import com.extremum.sharedmodels.annotation.DocumentationName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DocumentationName({"Agent", "Identity"})
public class IdentityRequestDto implements RequestDto {
    private boolean verified;
    private String locale;
    private String timezone;
    private Descriptor person;
}
