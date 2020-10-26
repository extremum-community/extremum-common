package io.extremum.watch.config;

import io.extremum.authentication.api.IdentityExtension;
import io.extremum.authentication.api.IdentityFinder;
import io.extremum.authentication.api.SecurityIdentity;
import io.extremum.watch.config.conditional.KafkaConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class WatchConfiguration {
    private final WatchProperties watchProperties;

    @Bean
    public ExecutorService watchEventsHandlingExecutor() {
        return Executors.newFixedThreadPool(watchProperties.getProcessingThreads(),
                new CustomizableThreadFactory("watch-events-"));
    }

    @Bean
    @ConditionalOnMissingBean
    IdentityFinder dummyIdentityFinder() {
        return principalId ->
            new SecurityIdentity() {
                @Override
                public String getExternalId() {
                    throw new UnsupportedOperationException("You should define a bean implementing IdentityFinder interface to identify the current user");
                }

                @Override
                public <T extends IdentityExtension> T getExtension() {
                    return null;
                }
            };
    }
}
