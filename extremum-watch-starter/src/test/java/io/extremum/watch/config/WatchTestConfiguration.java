package io.extremum.watch.config;

import io.extremum.security.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@EnableAutoConfiguration(exclude = MongoReactiveDataAutoConfiguration.class)
@ComponentScan("io.extremum.watch.end2end.fixture")
public class WatchTestConfiguration {
    @Bean
    public PrincipalSource principalSource() {
        return new PrincipalSource() {
            @Override
            public Optional<String> getPrincipal() {
                return Optional.of("Alex");
            }
        };
    }

    @Bean
    public DataSecurity everythingDataSecurity() {
        return new AllowEverythingForDataAccess();
    }

    @Bean
    public RoleChecker roleChecker() {
        return new AllowAnyRoleChecker();
    }
}
