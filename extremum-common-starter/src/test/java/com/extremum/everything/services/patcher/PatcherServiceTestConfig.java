package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.StubDtoConverter;
import com.extremum.common.dto.converters.services.DefaultDtoConversionService;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.mapper.JsonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class PatcherServiceTestConfig {
    @Bean
    public DefaultDtoConversionService dtoConversionService() {
        return new DefaultDtoConversionService(new ArrayList<>(), new StubDtoConverter());
    }

    @Bean
    public DtoConverter patchDtoConverter() {
        return new PatchModelConverter();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonObjectMapper.createWithDescriptors();
    }

    @Bean
    public TestPatcherService testPatcherService(DtoConversionService service) {
        return new TestPatcherService(service, objectMapper());
    }
}
