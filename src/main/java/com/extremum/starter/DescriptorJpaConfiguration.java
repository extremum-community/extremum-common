package com.extremum.starter;

import com.extremum.common.descriptor.factory.impl.PostgresqlDescriptorFactory;
import com.extremum.common.descriptor.service.PostgresqlDescriptorFactoryAccessorConfigurator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author rpuch
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class DescriptorJpaConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public PostgresqlDescriptorFactory postgresqlDescriptorFactory() {
        return new PostgresqlDescriptorFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresqlDescriptorFactoryAccessorConfigurator postgresqlDescriptorFactoryAccessorConfigurator() {
        return new PostgresqlDescriptorFactoryAccessorConfigurator(postgresqlDescriptorFactory());
    }

}
