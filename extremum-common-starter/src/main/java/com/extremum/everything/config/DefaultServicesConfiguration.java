package com.extremum.everything.config;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.service.MongoCommonService;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.everything.services.mongo.DefaultMongoGetterService;
import com.extremum.everything.services.mongo.DefaultMongoPatcherService;
import com.extremum.everything.services.mongo.DefaultMongoRemovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DefaultServicesConfiguration {
    @Bean
    public DefaultMongoGetterService<MongoCommonModel> mongoGetterService(List<MongoCommonService<? extends MongoCommonModel>> services) {
        return new DefaultMongoGetterService<>(services);
    }

    @Bean
    public DefaultMongoPatcherService<MongoCommonModel> mongoPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                                                            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator validator,
                                                                            List<MongoCommonService<? extends MongoCommonModel>> services, List<FromRequestDtoConverter<? extends MongoCommonModel, ? extends RequestDto>> dtoConverters) {
        return new DefaultMongoPatcherService<>(dtoConversionService, jsonMapper, emptyFieldDestroyer, validator, services, dtoConverters);
    }

    @Bean
    public DefaultMongoRemovalService<MongoCommonModel> mongoRemovalService(List<MongoCommonService<? extends MongoCommonModel>> services) {
        return new DefaultMongoRemovalService<>(services);
    }
}
