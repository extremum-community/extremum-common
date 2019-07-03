package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.ToRequestDtoConverter;

/**
 * @author rpuch
 */
class DtoConverterForModelWithServices
        implements ToRequestDtoConverter<MongoModelWithServices, RequestDtoForModelWithServices> {
    @Override
    public RequestDtoForModelWithServices convertToRequest(MongoModelWithServices model, ConversionConfig config) {
        return new RequestDtoForModelWithServices();
    }

    @Override
    public Class<RequestDtoForModelWithServices> getRequestDtoType() {
        return RequestDtoForModelWithServices.class;
    }

    @Override
    public String getSupportedModel() {
        return MongoModelWithServices.class.getSimpleName();
    }
}
