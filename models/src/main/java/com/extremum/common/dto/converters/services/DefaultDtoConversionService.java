package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.StubDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.ToResponseDtoConverter;
import com.extremum.common.dto.converters.annotations.DtoModelConverter;
import com.extremum.common.models.Model;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Getter
@Setter
public class DefaultDtoConversionService implements DtoConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDtoConversionService.class);

    private StubDtoConverter stubDtoConverter;
    // todo think about to how to fill the convertersMap
    private Map<String, List<? extends DtoConverter>> convertersMap;
    private boolean useStubConverter = true;

    public DefaultDtoConversionService(StubDtoConverter stubDtoConverter,
                                       Map<String, List<? extends DtoConverter>> convertersMap) {
        this.stubDtoConverter = stubDtoConverter;
        this.convertersMap = convertersMap;
    }

    @Override
    public DtoConverter determineConverter(Model model) {
        requireNonNull(model, "Model can't be null");

        Annotation[] annotations = model.getClass().getDeclaredAnnotations();

        return Arrays.stream(annotations)
                .filter(DtoModelConverter.class::isInstance)
                .map(DtoModelConverter.class::cast)
                .map(this::extractConverterFromAnnotation)
                .findAny()
                .orElse(null);
    }

    private DtoConverter extractConverterFromAnnotation(DtoModelConverter dtoModelConverter) {
        Class<? extends DtoConverter> foundConverterClass = dtoModelConverter.value();

        if (convertersMap.containsKey(foundConverterClass.getName())) {
            List<? extends DtoConverter> converters = convertersMap.get(foundConverterClass.getName());
            if (converters.size() != 1) {
                if (converters.isEmpty()) {
                    throw noConvertersFound(foundConverterClass);
                } else {
                    throw unexpectedConvertersCountFound(foundConverterClass, converters);
                }
            } else {
                return converters.get(0);
            }
        } else {
            if (useStubConverter) {
                return new StubDtoConverter();
            } else {
                throw noConvertersFound(foundConverterClass);
            }
        }
    }

    private RuntimeException unexpectedConvertersCountFound(Class<? extends DtoConverter> foundConverterClass, List<? extends DtoConverter> converters) {
        String message = format("Expected only one converter for class %s. But %s was found: %s",
                foundConverterClass.getName(), converters.size(), converters);
        LOGGER.error(message);
        return new RuntimeException(message);
    }

    private RuntimeException noConvertersFound(Class<? extends DtoConverter> foundConverterClass) {
        String message = format("No converters found for class %s", foundConverterClass.getName());
        LOGGER.error(message);
        return new RuntimeException(message);
    }

    @Override
    public DtoConverter determineConverterOrElseThrow(Model model, Supplier<? extends RuntimeException> exceptionSupplier) {
        DtoConverter converter = determineConverter(model);
        if (converter == null) {
            LOGGER.error("Unable to determine a converter for model {}", model.getModelName());
            throw exceptionSupplier.get();
        } else {
            return converter;
        }
    }

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        DtoConverter converter = determineConverter(model);

        if (converter == null) {
            LOGGER.error("Unable to determine converter for model {}: {}", model.getModelName(), model);
            converter = stubDtoConverter;
        }

        if (converter instanceof ToResponseDtoConverter) {
            return ((ToResponseDtoConverter) converter).convertToResponse(model, config);
        } else {
            String message = format("Found converter for a model %s is not a ToResponseDtoConverter instance",
                    model.getModelName());
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        DtoConverter converter = determineConverter(model);

        if (converter == null) {
            String message = format("Unable to determine converter for model %s: %s", model.getModelName(), model);

            LOGGER.error(message);

            throw new RuntimeException(message);
        } else {
            if (converter instanceof ToRequestDtoConverter) {
                return ((ToRequestDtoConverter) converter).convertToRequest(model, config);
            } else {
                String message = format("Found converter for a model %s is not a instance ToRequestDtoConverter",
                        model.getModelName());
                LOGGER.error(message);
                throw new RuntimeException(message);
            }
        }
    }
}
