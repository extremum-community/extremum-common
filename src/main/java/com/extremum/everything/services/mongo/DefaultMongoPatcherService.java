package com.extremum.everything.services.mongo;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelRequestDto;
import com.extremum.common.service.MongoCommonService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DefaultMongoPatcherService<M extends MongoCommonModel> extends AbstractPatcherService<M> implements DefaultMongoService<M> {
    private final List<MongoCommonService<? extends M>> services;
    private final List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters;

    public DefaultMongoPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                      EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
                                      List<MongoCommonService<? extends M>> services, List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
        super(dtoConversionService, jsonMapper, emptyFieldDestroyer, dtoValidator);
        this.services = services;
        this.dtoConverters = dtoConverters;
    }


    @Override
    protected M persist(PatchPersistenceContext<M> context, String modelName) {
        Class<? extends Model> modelClass = ModelClasses.getClassByModelName(modelName);
        boolean annotationPresent = modelClass.isAnnotationPresent(ModelRequestDto.class);
        if (!annotationPresent)
            throw new RuntimeException("No ModelRequestDto annotation on model with name " + modelName);
        Class<? extends RequestDto> requestDtoClass = modelClass.getAnnotation(ModelRequestDto.class).value();
        RequestDto requestDto = context.getRequestDto();
        FromRequestDtoConverter<? extends M, ? extends RequestDto> converter = dtoConverters
                .stream()
                .filter(dtoConverter -> dtoConverter.getRequestDtoType().equals(requestDtoClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find %s for a model %s", FromRequestDtoConverter.class.getSimpleName(), modelClass)));

//      We can eliminate this warning because converter cast his second generic parameter to the base wildcard class
        @SuppressWarnings("unchecked") M model = ((FromRequestDtoConverter<? extends M, RequestDto>) converter).convertFromRequest(requestDto);
        context.getOriginModel().copyServiceFieldsTo(model);

//      We can eliminate this warning because we cast service generic to the base class
        @SuppressWarnings("unchecked") M result = ((MongoCommonService<M>) findServiceByModel(services, model.getClass())).save(model);
        return result;
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
