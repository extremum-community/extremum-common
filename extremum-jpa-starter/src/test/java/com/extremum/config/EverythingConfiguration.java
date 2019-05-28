package com.extremum.config;

import com.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DefaultJpaServicesConfiguration.class, JpaRepositoriesConfiguration.class, CommonConfiguration.class})
public class EverythingConfiguration {
}
