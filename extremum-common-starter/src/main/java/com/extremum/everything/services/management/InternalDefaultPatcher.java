package com.extremum.everything.services.management;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

class InternalDefaultPatcher<M extends Model> extends AbstractPatcherService<M> {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;
    private final DefaultGetter<M> defaultGetter;
    private final List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters;

    InternalDefaultPatcher(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            CommonServices commonServices,
            ModelClasses modelClasses,
            DefaultGetter<M> defaultGetter,
            List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
        super(dtoConversionService, jsonMapper, emptyFieldDestroyer, dtoValidator);
        this.commonServices = commonServices;
        this.modelClasses = modelClasses;
        this.defaultGetter = defaultGetter;
        this.dtoConverters = dtoConverters;
    }

    @Override
    protected M persist(PatchPersistenceContext<M> context, String modelName) {
        Class<? extends Model> modelClass = modelClasses.getClassByModelName(modelName);
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
        @SuppressWarnings("unchecked")
        CommonService<?, M> commonService = (CommonService<?, M>) commonServices.findServiceByModel(model.getClass());
        return commonService.save(model);
    }

    @Override
    protected M findById(String internalId) {
        return defaultGetter.get(internalId);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
