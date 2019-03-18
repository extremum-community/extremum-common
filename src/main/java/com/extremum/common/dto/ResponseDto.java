package com.extremum.common.dto;

import java.time.ZonedDateTime;

/**
 * Base interface describes a response DTO
 */
public interface ResponseDto extends Dto {
    String getId();

    Long getVersion();

    ZonedDateTime getCreated();

    ZonedDateTime getModified();

    String getModel();
}
