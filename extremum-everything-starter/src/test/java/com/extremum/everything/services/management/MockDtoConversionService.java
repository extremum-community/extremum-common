package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.*;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;

import java.util.Optional;

/**
 * @author rpuch
 */
class MockDtoConversionService implements DtoConversionService {

    @Override
    public <M extends Model, D extends RequestDto> Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(
            Class<? extends M> modelClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <M extends Model, D extends RequestDto> Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(
            Class<? extends M> modelClass) {
        if (modelClass == MongoModelWithServices.class) {
            return Optional.of((ToRequestDtoConverter<M, D>) new DtoConverterForModelWithServices());
        }
        if (modelClass == MongoModelWithoutServices.class) {
            return Optional.of((ToRequestDtoConverter<M, D>) new DtoConverterForModelWithoutServices());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <M extends Model, D extends ResponseDto> Optional<ToResponseDtoConverter<M, D>> findToResponseDtoConverter(
            Class<? extends M> modelClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseDto convertUnknownToResponseDto(Model model, ConversionConfig config) {
        if (model instanceof MongoModelWithServices) {
            return new ResponseDtoForModelWithServices();
        }
        if (model instanceof MongoModelWithoutServices) {
            return new ResponseDtoForModelWithoutServices();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDto convertUnknownToRequestDto(Model model, ConversionConfig config) {
        if (model instanceof MongoModelWithServices) {
            return new RequestDtoForModelWithServices();
        }
        if (model instanceof MongoModelWithoutServices) {
            return new RequestDtoForModelWithoutServices();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends RequestDto> findRequestDtoType(Class<? extends Model> modelClass) {
        return findToRequestDtoConverter(modelClass)
                .orElseThrow(() -> new IllegalStateException("No to-request-converter to " + modelClass))
                .getRequestDtoType();
    }
}
