package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.*;
import io.extremum.common.exceptions.ConverterNotFoundException;
import io.extremum.common.models.Model;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@RequiredArgsConstructor
@Service
public class DefaultDtoConversionService implements DtoConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDtoConversionService.class);

    private final DtoConverters dtoConverters;
    private final StubDtoConverter stubDtoConverter;

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        ToResponseDtoConverter<Model, ResponseDto> converter = dtoConverters.<Model, ResponseDto>findToResponseDtoConverter(model.getClass())
                .orElseGet(() -> warnAndGetStubConverter(model));
        return converter.convertToResponse(model, config);
    }

    private ToResponseDtoConverter<Model, ResponseDto> warnAndGetStubConverter(Model model) {
        LOGGER.error("Unable to find a to-response-dto-converter for model {}: {}", model.getClass().getSimpleName(), model);
        @SuppressWarnings("unchecked")
        ToResponseDtoConverter<Model, ResponseDto> castConverter = this.stubDtoConverter;
        return castConverter;
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        ToRequestDtoConverter<Model, RequestDto> converter = findMandatoryToRequestConverter(model.getClass());
        return converter.convertToRequest(model, config);
    }

    @Override
    public <M extends Model, D extends RequestDto> M convertFromRequestDto(Class<? extends Model> modelClass, D dto) {
        FromRequestDtoConverter<M, D> converter = dtoConverters.<M, D>findFromRequestDtoConverter((Class<? extends M>) modelClass)
                .orElseThrow(() -> new ConverterNotFoundException(
                        format("Unable to find converter for model '%s'", modelClass.getSimpleName())));
        return converter.convertFromRequest(dto);
    }

    private ToRequestDtoConverter<Model, RequestDto> findMandatoryToRequestConverter(
            Class<? extends Model> modelClass) {
        return dtoConverters.<Model, RequestDto>findToRequestDtoConverter(modelClass)
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
