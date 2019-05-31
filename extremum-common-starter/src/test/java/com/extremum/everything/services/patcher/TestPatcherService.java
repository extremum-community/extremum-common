package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.everything.services.AbstractPatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestPatcherService extends AbstractPatcherService<PatchModel> {

    protected TestPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
        super(dtoConversionService, jsonMapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected PatchModel persist(PatchPersistenceContext<PatchModel> context, String modelName) {
        return ((FromRequestDtoConverter<PatchModel, PatchModelRequestDto>)
                getDtoConversionService().determineConverter(PatchModel.class))
                .convertFromRequest((PatchModelRequestDto) context.getRequestDto());
    }

    @Override
    protected PatchModel findById(String id) {
        return new PatchModel("patch");
    }

    @Override
    public String getSupportedModel() {
        return "patchModel";
    }
}
