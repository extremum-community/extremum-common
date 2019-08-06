package com.extremum.watch.controller;

import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author rpuch
 */
@Configuration
@AutoConfigureWebMvc
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class WatchControllersTestConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new SystemJsonObjectMapper(Mockito.mock(MapperDependencies.class));
    }
}
