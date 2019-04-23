package com.extremum.common.dto;

import com.extremum.common.descriptor.Descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public abstract class CommonResponseDto extends AbstractResponseDto {
    public Descriptor id;

    @Override
    public String getId() {
        return Optional.ofNullable(id).map(Descriptor::getExternalId).orElse(null);
    }
}
