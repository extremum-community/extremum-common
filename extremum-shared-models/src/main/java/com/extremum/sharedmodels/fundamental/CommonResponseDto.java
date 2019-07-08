package com.extremum.sharedmodels.fundamental;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.dto.ResponseDto;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Setter
@ToString
@DocumentationName("Object")
public abstract class CommonResponseDto implements ResponseDto {
    /**
     * The unique ID of the object
     */
    private Descriptor id;

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
