package com.extremum.config;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.services.DefaultJpaGetterService;
import com.extremum.services.DefaultJpaPatcherService;
import com.extremum.services.DefaultJpaRemovalService;
import com.extremum.models.PostgresBasicModel;
import com.extremum.services.PostgresBasicService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DefaultJpaServicesConfiguration {@Bean
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
