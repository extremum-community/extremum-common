package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.*;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Getter
@Setter
@Service
public class DefaultDtoConversionService implements DtoConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDtoConversionService.class);

    private StubDtoConverter stubDtoConverter;
    private List<DtoConverter> converters;
    private boolean useStubConverter = true;

    public DefaultDtoConversionService(@Autowired(required = false) List<DtoConverter> converters,
                                       StubDtoConverter stubDtoConverter) {
        this.converters = ofNullable(converters).orElseGet(ArrayList::new);
        this.stubDtoConverter = stubDtoConverter;
    }

    @Override
    public DtoConverter determineConverter(Class<? extends Model> modelClass) {
        requireNonNull(modelClass, "Model class can't be null");

        for (DtoConverter converter : converters) {
            if (ModelUtils.hasModelName(modelClass)) {
                if (ModelUtils.getModelName(modelClass).equalsIgnoreCase(converter.getSupportedModel())) {
                    return converter;
                }
            }
        }
        return null;
    }

    @Override
    public DtoConverter determineConverterOrElseThrow(Model model, Supplier<? extends RuntimeException> exceptionSupplier) {
        DtoConverter converter = determineConverter(model.getClass());
        if (converter == null) {
            LOGGER.error("Unable to determine a converter for model {}", model.getClass().getSimpleName());
            throw exceptionSupplier.get();
        } else {
            return converter;
        }
    }

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        DtoConverter converter = determineConverter(model.getClass());

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
        DtoConverter converter = determineConverter(model.getClass());

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
