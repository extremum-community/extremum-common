package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;

public class PatchModelConverter implements ToRequestDtoConverter<PatchModel, PatchModelRequestDto>, FromRequestDtoConverter<PatchModel, PatchModelRequestDto> {
    @Override
    public PatchModelRequestDto convertToRequest(PatchModel model, ConversionConfig config) {
        return new PatchModelRequestDto(model.getName());
    }

    @Override
    public PatchModel convertFromRequest(PatchModelRequestDto dto) {
        return new PatchModel(dto.getName());
    }

    @Override
    public Class<? extends PatchModelRequestDto> getRequestDtoType() {
        return PatchModelRequestDto.class;
    }

    @Override
    public String getSupportedModel() {
        return "patchModel";
    }
}
