package com.extremum.everything.services.defaultservices;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.security.EverythingDataSecurity;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelClasses;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

public class DefaultPatcherImpl<M extends Model> implements DefaultPatcher<M> {
    private final InternalDefaultPatcher<M> internalPatcher;

    public DefaultPatcherImpl(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            CommonServices commonServices,
            ModelClasses modelClasses,
            DefaultGetter<M> defaultGetter,
            EverythingDataSecurity dataSecurity) {
        internalPatcher = new InternalDefaultPatcher<>(dtoConversionService, jsonMapper, emptyFieldDestroyer,
                dtoValidator, commonServices, modelClasses, defaultGetter, dataSecurity);
    }

    @Override
    public M patch(String id, JsonPatch patch) {
        return internalPatcher.patch(id, patch);
    }
}
