package io.extremum.dynamic.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class JsonDynamicModelResponseDto extends CommonResponseDto {
    private final String model;
    private JsonNode data;
}
