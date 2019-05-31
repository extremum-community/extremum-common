package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.everything.services.AbstractPatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.String.format;

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
        if ("id".equals(id)) {
            return new PatchModel("patch");
        }
        throw new ModelNotFoundException(format("Cannot find model with id %s", id));
    }

    @Override
    public String getSupportedModel() {
        return "patchModel";
    }
}
