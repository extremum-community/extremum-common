package com.extremum.jpa.config;

import com.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, JpaRepositoriesConfiguration.class})
public class JpaConfiguration {
}
