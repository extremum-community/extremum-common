package com.extremum.watch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude
public class TextWatchEventResponseDto {
    private final PatchedObjectMetadata object;
    @JsonRawValue
    private final String patch;
}
