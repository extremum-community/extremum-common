package com.extremum.everything.services.patcher;

import com.extremum.common.dto.RequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PatchModelRequestDto implements RequestDto {
    private String name;
}
