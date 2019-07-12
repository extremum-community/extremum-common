package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.everything.services.AbstractPatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.String.format;

public class TestPatcherService extends AbstractPatcherService<PatchModel> {
    private final DtoConversionService dtoConversionService;

    protected TestPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
        super(dtoConversionService, jsonMapper);

        this.dtoConversionService = dtoConversionService;
    }

    @Override
    protected PatchModel persist(PatchPersistenceContext<PatchModel> context) {
        return findConverter().convertFromRequest((PatchModelRequestDto) context.getPatchedDto());
    }

    @SuppressWarnings("unchecked")
    private FromRequestDtoConverter<PatchModel, PatchModelRequestDto> findConverter() {
        return (FromRequestDtoConverter<PatchModel, PatchModelRequestDto>)
                dtoConversionService.determineConverter(PatchModel.class);
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
