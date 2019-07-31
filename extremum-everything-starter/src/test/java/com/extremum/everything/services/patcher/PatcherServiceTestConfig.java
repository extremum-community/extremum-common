package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.StubDtoConverter;
import com.extremum.common.dto.converters.services.DefaultDtoConversionService;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.dto.converters.services.DtoConvertersCollection;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.everything.MockedMapperDependencies;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class PatcherServiceTestConfig {
    @Bean
    public MapperDependencies mapperDependencies() {
        return new MockedMapperDependencies();
    }

    @Bean
    public DtoConvertersCollection dtoConverters() {
        return new DtoConvertersCollection(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Bean
    public DefaultDtoConversionService dtoConversionService() {
        return new DefaultDtoConversionService(dtoConverters(), new StubDtoConverter());
    }

    @Bean
    public DtoConverter patchDtoConverter() {
        return new PatchModelConverter();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new SystemJsonObjectMapper(mapperDependencies());
    }

    @Bean
    public TestPatcherService testPatcherService(DtoConversionService service) {
        return new TestPatcherService(service, objectMapper());
    }
}