package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.ToResponseDtoConverter;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
@Service
public class DtoConvertersCollection implements DtoConverters {
    @Getter
    private final List<FromRequestDtoConverter<?, ?>> fromRequestConverters;
    @Getter
    private final List<ToRequestDtoConverter<?, ?>> toRequestConverters;
    private final List<ToResponseDtoConverter<?, ?>> toResponseConverters;

    @Override
    public <M extends Model, D extends RequestDto> Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(
            Class<? extends M> modelClass) {
        return findConverter(modelClass, fromRequestConverters)
                .map(converter -> (FromRequestDtoConverter<M, D>) converter);
    }

    @Override
    public <M extends Model, D extends RequestDto> Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(
            Class<? extends M> modelClass) {
        return findConverter(modelClass, toRequestConverters)
                .map(converter -> (ToRequestDtoConverter<M, D>) converter);
    }

    @Override
    public <M extends Model, D extends ResponseDto> Optional<ToResponseDtoConverter<M, D>> findToResponseDtoConverter(
            Class<? extends M> modelClass) {
        return findConverter(modelClass, toResponseConverters)
                .map(converter -> (ToResponseDtoConverter<M, D>) converter);
    }

    private <T extends DtoConverter> Optional<T> findConverter(Class<? extends Model> modelClass, List<T> converters) {
        if (!ModelUtils.hasModelName(modelClass)) {
            return Optional.empty();
        }

        String modelName = ModelUtils.getModelName(modelClass);

        for (T converter : converters) {
            if (modelName.equals(converter.getSupportedModel())) {
                return Optional.of(converter);
            }
        }

        return Optional.empty();
    }
}
