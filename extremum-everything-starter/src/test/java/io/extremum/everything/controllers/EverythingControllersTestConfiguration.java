package io.extremum.everything.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.everything.MockedMapperDependencies;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureWebMvc
@AutoConfigureBefore({WebMvcAutoConfiguration.class, WebFluxAutoConfiguration.class})
public class EverythingControllersTestConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new SystemJsonObjectMapper(new MockedMapperDependencies());
    }
}
