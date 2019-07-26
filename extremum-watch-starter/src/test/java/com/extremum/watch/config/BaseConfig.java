package com.extremum.watch.config;

import com.extremum.everything.security.AllowAnyRoleChecker;
import com.extremum.everything.security.AllowEverythingForDataAccess;
import com.extremum.everything.security.EverythingDataSecurity;
import com.extremum.everything.security.RoleChecker;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class BaseConfig {
    @Bean
    public EverythingDataSecurity everythingDataSecurity() {
        return new AllowEverythingForDataAccess();
    }

    @Bean
    public RoleChecker roleChecker() {
        return new AllowAnyRoleChecker();
    }
}
