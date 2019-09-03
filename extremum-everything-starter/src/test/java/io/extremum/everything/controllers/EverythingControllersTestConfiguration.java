package io.extremum.everything.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.everything.MockedMapperDependencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EverythingControllersTestConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new SystemJsonObjectMapper(new MockedMapperDependencies());
    }
}