package com.extremum.everything.services.defaultservices;

import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelClasses;
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
    protected M persist(PatchPersistenceContext<M> context) {
        FromRequestDtoConverter<? extends M, RequestDto> converter = findConverter(context);
        M model = converter.convertFromRequest(context.getPatchedDto());
        context.getOriginalModel().copyServiceFieldsTo(model);

        CommonService<M> commonService = findService(context);
        return commonService.save(model);
    }

    private Class<M> modelClass(PatchPersistenceContext<M> context) {
        return modelClasses.getClassByModelName(context.modelName());
    }

    private FromRequestDtoConverter<? extends M, RequestDto> findConverter(
            PatchPersistenceContext<M> context) {
        FromRequestDtoConverter<? extends M, ? extends RequestDto> converter = dtoConverters
                .stream()
                .filter(dtoConverter -> context.modelName().equals(dtoConverter.getSupportedModel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Cannot find %s for a model %s", FromRequestDtoConverter.class.getSimpleName(),
                                modelClass(context))));

//      We can eliminate this warning because converter cast his second generic parameter to the base wildcard class
        @SuppressWarnings("unchecked")
        FromRequestDtoConverter<? extends M, RequestDto> castConverter = (FromRequestDtoConverter<? extends M, RequestDto>) converter;
        return castConverter;
    }

    private CommonService<M> findService(PatchPersistenceContext<M> context) {
        return commonServices.findServiceByModel(modelClass(context));
    }

    @Override
    protected M findById(String internalId) {
        return defaultGetter.get(internalId);
    }

    @Override
    public String getSupportedModel() {
        throw new UnsupportedOperationException(
                "This method should not be called, we only extend AbstractPatcherService for convenience");
    }
}
