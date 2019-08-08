package com.extremum.watch.config;

import com.extremum.everything.config.EverythingEverythingConfiguration;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Configuration
@AutoConfigureAfter(EverythingEverythingConfiguration.class)
@EnableAspectJAutoProxy
@EnableConfigurationProperties(WatchProperties.class)
@EnableMongoRepositories("com.extremum.watch.repositories")
@ComponentScan("com.extremum.watch")
@Import(KafkaConfiguration.class)
public class WatchConfiguration {
    private final WatchProperties watchProperties;

    @Bean
    public ExecutorService watchEventsHandlingExecutor() {
        return Executors.newFixedThreadPool(watchProperties.getProcessingThreads(),
                new CustomizableThreadFactory("watch-events-"));
    }
}
