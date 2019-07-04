package com.extremum.sharedmodels.personal;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.stucts.IdOrObjectStruct;
import com.extremum.sharedmodels.annotation.DocumentationName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DocumentationName({"Agent", "Identity"})
public class IdentityResponseDto implements ResponseDto {
    public static final String MODEL_NAME = "Identity";

    private Descriptor id;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private Long version;
    private boolean verified;
    private Locale locale;
    private String timezone;
    private IdOrObjectStruct<Descriptor, Person> person;

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

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
