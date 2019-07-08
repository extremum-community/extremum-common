package com.extremum.everything.services.defaultservices;

import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

import java.util.List;

public class DefaultPatcherImpl<M extends Model> implements DefaultPatcher<M> {
    private final InternalDefaultPatcher<M> internalPatcher;

    public DefaultPatcherImpl(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            CommonServices commonServices,
            ModelClasses modelClasses,
            DefaultGetter<M> defaultGetter,
            List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
        internalPatcher = new InternalDefaultPatcher<>(dtoConversionService, jsonMapper, emptyFieldDestroyer,
                dtoValidator, commonServices, modelClasses, defaultGetter, dtoConverters);
    }

    @Override
    public M patch(String id, JsonPatch patch) {
        return internalPatcher.patch(id, patch);
    }
}
