package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.*;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Setter
@Service
public class DefaultDtoConversionService implements DtoConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDtoConversionService.class);

    private final StubDtoConverter stubDtoConverter;
    @Getter
    private final List<DtoConverter> converters;

    public DefaultDtoConversionService(List<DtoConverter> converters,
                                       StubDtoConverter stubDtoConverter) {
        this.converters = converters;
        this.stubDtoConverter = stubDtoConverter;
    }

    @Override
    public DtoConverter findConverter(Class<? extends Model> modelClass) {
        requireNonNull(modelClass, "Model class can't be null");

        if (!ModelUtils.hasModelName(modelClass)) {
            return null;
        }

        String modelName = ModelUtils.getModelName(modelClass);
        for (DtoConverter converter : converters) {
            if (modelName.equalsIgnoreCase(converter.getSupportedModel())) {
                return converter;
            }
        }
        return null;
    }

    @Override
    public DtoConverter findConverterOrThrow(Model model, Supplier<? extends RuntimeException> exceptionSupplier) {
        DtoConverter converter = findConverter(model.getClass());
        if (converter == null) {
            LOGGER.error("Unable to determine a converter for model {}", model.getClass().getSimpleName());
            throw exceptionSupplier.get();
        } else {
            return converter;
        }
    }

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        DtoConverter converter = findConverter(model.getClass());

        if (converter == null) {
            LOGGER.error("Unable to determine converter for model {}: {}", model.getClass().getSimpleName(), model);
            converter = stubDtoConverter;
        }

        if (converter instanceof ToResponseDtoConverter) {
            return ((ToResponseDtoConverter) converter).convertToResponse(model, config);
        } else {
            String message = format("Found converter for a model %s is not a ToResponseDtoConverter instance",
                    model.getClass().getSimpleName());
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        DtoConverter converter = findConverter(model.getClass());

        if (converter == null) {
            String message = format("Unable to determine converter for model %s: %s", model.getClass().getSimpleName(), model);

            LOGGER.error(message);

            throw new RuntimeException(message);
        } else {
            if (converter instanceof ToRequestDtoConverter) {
                return ((ToRequestDtoConverter) converter).convertToRequest(model, config);
            } else {
                String message = format("Found converter for a model %s is not a instance ToRequestDtoConverter",
                        model.getClass().getSimpleName());
                LOGGER.error(message);
                throw new RuntimeException(message);
            }
        }
    }
}
