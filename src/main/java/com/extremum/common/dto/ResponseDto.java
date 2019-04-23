package com.extremum.common.dto;

import com.extremum.common.descriptor.Descriptor;

import java.time.ZonedDateTime;

/**
 * Base interface describes a response DTO
 */
public interface ResponseDto extends Dto {
    Descriptor getId();

    Long getVersion();

    ZonedDateTime getCreated();

    ZonedDateTime getModified();

    String getModel();
}
