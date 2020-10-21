package io.extremum.watch.config;

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
@AutoConfigureAfter(name = {"io.extremum.everything.regular.config.EverythingEverythingConfiguration", "io.extremum.everything.reactive.config.ReactiveEverythingConfiguration"})
@EnableAspectJAutoProxy
@EnableConfigurationProperties(WatchProperties.class)
@EnableMongoRepositories("io.extremum.watch.repositories")
@ComponentScan(
        value = "io.extremum.watch",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "io\\.extremum\\.watch\\.config\\.conditional\\..*"
        )
)
@Import(KafkaConfiguration.class)
public class WatchConfiguration {
    private final WatchProperties watchProperties;

    @Bean
    public ExecutorService watchEventsHandlingExecutor() {
        return Executors.newFixedThreadPool(watchProperties.getProcessingThreads(),
                new CustomizableThreadFactory("watch-events-"));
    }
}
