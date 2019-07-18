package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.everything.security.AllowEverythingForDataAccess;
import com.extremum.everything.services.AbstractPatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.String.format;

public class TestPatcherService extends AbstractPatcherService<PatchModel> {

    protected TestPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
        super(dtoConversionService, jsonMapper, new AllowEverythingForDataAccess());
    }

    @Override
    protected PatchModel persist(PatchPersistenceContext<PatchModel> context) {
        return context.getPatchedModel();
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
