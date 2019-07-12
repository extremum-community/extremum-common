package com.extremum.everything.services.defaultservices;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelClasses;
import com.fasterxml.jackson.databind.ObjectMapper;

class InternalDefaultPatcher<M extends Model> extends AbstractPatcherService<M> {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;
    private final DefaultGetter<M> defaultGetter;

    InternalDefaultPatcher(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            CommonServices commonServices,
            ModelClasses modelClasses,
            DefaultGetter<M> defaultGetter) {
        super(dtoConversionService, jsonMapper, emptyFieldDestroyer, dtoValidator);
        this.commonServices = commonServices;
        this.modelClasses = modelClasses;
        this.defaultGetter = defaultGetter;
    }

    @Override
    protected M persist(PatchPersistenceContext<M> context) {
        CommonService<M> commonService = findService(context);
        return commonService.save(context.getPatchedModel());
    }

    private Class<M> modelClass(PatchPersistenceContext<M> context) {
        return modelClasses.getClassByModelName(context.modelName());
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
