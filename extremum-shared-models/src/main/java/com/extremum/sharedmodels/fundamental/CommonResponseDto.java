package com.extremum.sharedmodels.fundamental;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.sharedmodels.annotation.DocumentationName;

import java.time.ZonedDateTime;

@DocumentationName("Object")
public abstract class CommonResponseDto implements ResponseDto {
    /**
     * The unique ID of the object
     */
    public Descriptor id;

    /**
     * Date/time of object creation in ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss.SSSZ)
     */
    public ZonedDateTime created;

    /**
     * Date/time of object's last modification in ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss.SSSZ)
     */
    public ZonedDateTime modified;

    /**
     * The object's version
     */
    public Long version;

    @Override
    public Descriptor getId() {
        return id;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public ZonedDateTime getCreated() {
        return created;
    }

    @Override
    public ZonedDateTime getModified() {
        return modified;
    }
}
