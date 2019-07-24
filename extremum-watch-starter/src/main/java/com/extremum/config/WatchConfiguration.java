package com.extremum.config;

import com.extremum.everything.config.EverythingEverythingConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(EverythingEverythingConfiguration.class)
@EnableConfigurationProperties(SubscriptionProperties.class)
@ComponentScan("com.extremum.subscription")
public class WatchConfiguration {
}
