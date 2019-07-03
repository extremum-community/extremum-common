package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.DtoConverter;

public class PatchModelDtoConverter implements DtoConverter {
    @Override
    public String getSupportedModel() {
        return "patchModel";
    }
}
