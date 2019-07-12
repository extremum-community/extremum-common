package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;

import java.util.Optional;

/**
 * @author rpuch
 */
class MockDtoConversionService implements DtoConversionService {
    @Override
    public DtoConverter findConverter(Class<? extends Model> modelClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <M extends Model, D extends RequestDto> Optional<FromRequestDtoConverter<M, D>> findFromRequestDtoConverter(
            Class<? extends M> modelClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <M extends Model, D extends RequestDto> Optional<ToRequestDtoConverter<M, D>> findToRequestDtoConverter(
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
        throw new UnsupportedOperationException();
    }
}
