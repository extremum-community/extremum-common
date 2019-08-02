package com.extremum.watch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude
public class TextWatchEventNotificationDto {
    private final String operationType;
    private final String updateBody;
    private final Collection<String> subscribers;
}
