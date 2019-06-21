package com.extremum.everything.services.mongo;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.BasicModel;
import com.extremum.common.service.CommonService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

class InternalDefaultPatcher<M extends BasicModel<?>>
        extends AbstractPatcherService<M> implements DefaultService<M> {
    private final List<CommonService<?, ? extends M>> services;
    private final List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters;

    InternalDefaultPatcher(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            List<CommonService<?, ? extends M>> services,
            List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
        super(dtoConversionService, jsonMapper, emptyFieldDestroyer, dtoValidator);
        this.services = services;
        this.dtoConverters = dtoConverters;
    }


    @Override
    protected M persist(PatchPersistenceContext<M> context, String modelName) {
        Class<? extends BasicModel<?>> modelClass = (Class<? extends BasicModel<?>>) ModelClasses.getClassByModelName(modelName);
        RequestDto requestDto = context.getRequestDto();
        FromRequestDtoConverter<? extends M, ? extends RequestDto> converter = dtoConverters
                .stream()
                .filter(dtoConverter -> modelName.equals(dtoConverter.getSupportedModel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find %s for a model %s", FromRequestDtoConverter.class.getSimpleName(), modelClass)));

//      We can eliminate this warning because converter cast his second generic parameter to the base wildcard class
        @SuppressWarnings("unchecked") M model = ((FromRequestDtoConverter<? extends M, RequestDto>) converter).convertFromRequest(requestDto);
        context.getOriginModel().copyServiceFieldsTo(model);

//      We can eliminate this warning because we cast service generic to the base class
        @SuppressWarnings("unchecked") CommonService<?, M> commonService = (CommonService<?, M>) findServiceByModel(services, model.getClass());
        return commonService.save(model);
    }

    @Override
    protected M findById(String internalId) {
        return getById(services, internalId);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
