package com.extremum.jpa.everything;

import com.extremum.everything.config.listener.ModelClassesInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@TestConfiguration
public class TestConfig {
    @Bean
    public ModelClassesInitializer initializer() {
        return new ModelClassesInitializer(Collections.singletonList("com.extremum.jpa.everything"));
    }
}