package com.extremum.everything.services.patcher;

import com.extremum.common.dto.RequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PatchModelRequestDto implements RequestDto {
    @NotEmpty
    private String name;
}
