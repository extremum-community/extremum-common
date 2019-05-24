package com.extremum.everything.services.jpa;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.service.PostgresCommonService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.AbstractPatcherService;
import com.extremum.everything.services.RequestDtoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DefaultJpaPatcherService<M extends PostgresCommonModel> extends AbstractPatcherService<M> implements DefaultJpaService<M> {
    private final List<PostgresCommonService<? extends M>> services;
    private final List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters;

    public DefaultJpaPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                    EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
                                    List<PostgresCommonService<? extends M>> services,
                                    List<FromRequestDtoConverter<? extends M, ? extends RequestDto>> dtoConverters) {
        super(dtoConversionService, jsonMapper, emptyFieldDestroyer, dtoValidator);
        this.services = services;
        this.dtoConverters = dtoConverters;
    }

    @Override
    protected M persist(PatchPersistenceContext<M> context, String modelName) {
        Class<? extends Model> modelClass = ModelClasses.getClassByModelName(modelName);
        RequestDto requestDto = context.getRequestDto();
        FromRequestDtoConverter<? extends M, ? extends RequestDto> converter = dtoConverters
                .stream()
                .filter(dtoConverter -> modelName.equals(dtoConverter.getSupportedModel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find %s for a model %s", FromRequestDtoConverter.class.getSimpleName(), modelClass)));

//      We can eliminate this warning because converter cast his second generic parameter to the base wildcard class
        @SuppressWarnings("unchecked") M model = ((FromRequestDtoConverter<? extends M, RequestDto>) converter).convertFromRequest(requestDto);
        mergeServiceFields(context.getOriginModel(), model);

//      We can eliminate this warning because we cast service generic to the base class
        @SuppressWarnings("unchecked") M result = ((PostgresCommonService<M>) findServiceByModel(services, model.getClass())).save(model);
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
