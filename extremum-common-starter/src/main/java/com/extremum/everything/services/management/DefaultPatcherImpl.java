package com.extremum.everything.services.management;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.BasicModel;
import com.extremum.common.service.CommonService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

import java.util.List;

public class DefaultPatcherImpl<M extends BasicModel<?>> implements DefaultPatcher<M> {
    private final InternalDefaultPatcher<M> internalPatcher;

    public DefaultPatcherImpl(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            List<CommonService<?, ? extends M>> services,
            CommonServices commonServices,
            ModelClasses modelClasses,
            List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
        internalPatcher = new InternalDefaultPatcher<>(dtoConversionService, jsonMapper, emptyFieldDestroyer,
                dtoValidator, services, commonServices, modelClasses, dtoConverters);
    }

    @Override
    public M patch(String id, JsonPatch patch) {
        return internalPatcher.patch(id, patch);
    }
}
