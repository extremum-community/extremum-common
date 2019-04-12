package com.extremum.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class AbstractResponseDto implements ResponseDto {
    /**
     * The name of model that specifies the object structure
     */
    private String model;

    /**
     * Date/time of object creation in ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss.SSSZ)
     */
    private ZonedDateTime created;

    /**
     * Date/time of object's last modification in ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss.SSSZ)
     */
    private ZonedDateTime modified;

    /**
     * The object's version
     */
    private Long version;

}
