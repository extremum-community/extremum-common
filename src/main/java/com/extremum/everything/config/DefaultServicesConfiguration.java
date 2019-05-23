package com.extremum.everything.config;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.service.MongoCommonService;
import com.extremum.common.service.PostgresBasicService;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.everything.services.jpa.DefaultJpaGetterService;
import com.extremum.everything.services.jpa.DefaultJpaPatcherService;
import com.extremum.everything.services.jpa.DefaultJpaRemovalService;
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

    @Bean
    public DefaultJpaGetterService<PostgresBasicModel> jpaGetterService(List<PostgresBasicService<? extends PostgresBasicModel>> services) {
        return new DefaultJpaGetterService<>(services);
    }

    @Bean
    public DefaultJpaRemovalService<PostgresBasicModel> jpaRemovalService(List<PostgresBasicService<? extends PostgresBasicModel>> services) {
        return new DefaultJpaRemovalService<>(services);
    }

    @Bean
    public DefaultJpaPatcherService<PostgresBasicModel> jpaPatcherService(DtoConversionService dtoConversionService,
            ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator validator,
            List<PostgresBasicService<? extends PostgresBasicModel>> services,
            List<FromRequestDtoConverter<? extends PostgresBasicModel, ? extends RequestDto>> dtoConverters) {
        return new DefaultJpaPatcherService<>(dtoConversionService, jsonMapper, emptyFieldDestroyer, validator,
                services, dtoConverters);
    }
}
