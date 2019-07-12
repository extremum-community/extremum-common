package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.*;
import com.extremum.common.exceptions.ConverterNotFoundException;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Setter
@RequiredArgsConstructor
@Service
public class DefaultDtoConversionService implements DtoConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDtoConversionService.class);

    @Getter
    private final List<FromRequestDtoConverter<?, ?>> fromRequestConverters;
    @Getter
    private final List<ToRequestDtoConverter<?, ?>> toRequestConverters;
    private final List<ToResponseDtoConverter<?, ?>> toResponseConverters;
    private final StubDtoConverter stubDtoConverter;

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

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        ToResponseDtoConverter<Model, ResponseDto> converter = this.<Model, ResponseDto>findToResponseDtoConverter(model.getClass())
                .orElse(warnAndGetStubConverter(model));
        return converter.convertToResponse(model, config);
    }

    private ToResponseDtoConverter<Model, ResponseDto> warnAndGetStubConverter(Model model) {
        LOGGER.error("Unable to find a to-response-dto-converter for model {}: {}", model.getClass().getSimpleName(), model);
        return stubDtoConverter;
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        ToRequestDtoConverter<Model, RequestDto> converter = findMandatoryToRequestConverter(model.getClass());
        return converter.convertToRequest(model, config);
    }

    @Override
    public <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<? extends Model> modelClass, D dto) {
        FromRequestDtoConverter<M, D> converter = this.<M, D>findFromRequestDtoConverter((Class<? extends M>) modelClass)
                .orElseThrow(() -> new ConverterNotFoundException(
                        format("Unable to find converter for model '%s'", modelClass.getSimpleName())));
        return converter.convertFromRequest(dto);
    }

    private ToRequestDtoConverter<Model, RequestDto> findMandatoryToRequestConverter(
            Class<? extends Model> modelClass) {
        return this.<Model, RequestDto>findToRequestDtoConverter(modelClass)
                    .orElseThrow(
                            () -> new ConverterNotFoundException(
                                    format("Unable to find converter for model '%s'", modelClass.getSimpleName()))
                    );
    }

    @Override
    public Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass) {
        return findMandatoryToRequestConverter(modelClass).getRequestDtoType();
    }
}
