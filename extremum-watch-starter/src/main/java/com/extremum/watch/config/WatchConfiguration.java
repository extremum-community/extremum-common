package com.extremum.watch.config;

import com.extremum.everything.config.EverythingEverythingConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@AutoConfigureAfter(EverythingEverythingConfiguration.class)
@EnableConfigurationProperties(SubscriptionProperties.class)
@EnableMongoRepositories("com.extremum.watch.repositories")
@ComponentScan("com.extremum.watch")
@Import(KafkaConfiguration.class)
public class WatchConfiguration {
}
