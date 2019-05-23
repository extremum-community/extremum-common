package com.extremum.everything.services.jpa;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.models.annotation.ModelRequestDto;
import com.extremum.common.service.PostgresBasicService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DefaultJpaPatcherService<M extends PostgresBasicModel> extends AbstractPatcherService<M>
        implements DefaultJpaService<M> {
    private final List<PostgresBasicService<? extends M>> services;
    private final List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters;

    public DefaultJpaPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                    EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
                                    List<PostgresBasicService<? extends M>> services,
                                    List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
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
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find %s for a model %s",
                        FromRequestDtoConverter.class.getSimpleName(), modelClass)));

//      We can eliminate this warning because converter cast his second generic parameter to the base wildcard class
        @SuppressWarnings("unchecked") M model = ((FromRequestDtoConverter<? extends M, RequestDto>) converter).convertFromRequest(requestDto);
        context.getOriginModel().mergeServiceFieldsTo(model);

//      We can eliminate this warning because we cast service generic to the base class
        @SuppressWarnings("unchecked") M result = ((PostgresBasicService<M>) findServiceByModel(services, model.getClass())).save(model);
        return result;
    }

    @Override
    protected M findById(String id) {
        return getById(services, id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
