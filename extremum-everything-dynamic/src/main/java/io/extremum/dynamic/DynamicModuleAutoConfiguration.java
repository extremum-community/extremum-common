package io.extremum.dynamic;

import io.extremum.dynamic.resources.github.GithubAccessOptions;
import io.extremum.dynamic.resources.github.GithubResourceConfiguration;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.CachingGithubNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.MemoryNetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntJsonDynamicModelValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GithubSchemaProperties.class})
@ComponentScan(basePackages = "io.extremum.dynamic")
public class DynamicModuleAutoConfiguration {
    @Bean
    public NetworkntCacheManager networkntCacheManager() {
        return new MemoryNetworkntCacheManager();
    }

    @Bean
    public GithubNetworkntSchemaProvider githubNetworkntSchemaProvider(GithubSchemaProperties githubSchemaProperties) {
        GithubResourceConfiguration githubResConfig = new GithubResourceConfiguration(
                githubSchemaProperties.getOwner(),
                githubSchemaProperties.getRepo(),
                githubSchemaProperties.getSchemaPath(),
                githubSchemaProperties.getRef()
        );

        GithubAccessOptions gihubAccessOpts = new GithubAccessOptions(
                githubSchemaProperties.getToken()
        );

        return new GithubNetworkntSchemaProvider(JsonSchemaType.V2019_09, githubResConfig, gihubAccessOpts);
    }

    @Bean
    public CachingGithubNetworkntSchemaProvider cachingGithubNetworkntSchemaProvider(NetworkntCacheManager schemaCacheManager,
                                                                       GithubNetworkntSchemaProvider githubSchemaProvider) {
        return new CachingGithubNetworkntSchemaProvider(schemaCacheManager, githubSchemaProvider);
    }

    @Bean
    public JsonDynamicModelValidator jsonDynamicModelValidator(CachingGithubNetworkntSchemaProvider provider) {
        return new NetworkntJsonDynamicModelValidator(provider);
    }
}
