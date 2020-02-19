package io.extremum.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.dao.MongoVersionedDynamicModelDao;
import io.extremum.dynamic.resources.github.GithubAccessOptions;
import io.extremum.dynamic.resources.github.GithubResourceConfiguration;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.CachingGithubNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.MemoryNetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import io.extremum.dynamic.server.impl.GithubWebhookListenerHttpSchemaServer;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.impl.DefaultDateTypesNormalizer;
import io.extremum.dynamic.services.impl.DefaultDatesProcessor;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntJsonDynamicModelValidator;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

import java.util.Collection;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty("github.schema.owner")
@EnableConfigurationProperties({GithubSchemaProperties.class})
@ComponentScan(basePackages = "io.extremum.dynamic")
public class DynamicModuleAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public NetworkntCacheManager networkntCacheManager() {
        return new MemoryNetworkntCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public NetworkntSchemaProvider networkntSchemaProvider(GithubSchemaProperties githubSchemaProperties, NetworkntCacheManager cacheManager) {
        GithubResourceConfiguration githubResConfig = new GithubResourceConfiguration(
                githubSchemaProperties.getOwner(),
                githubSchemaProperties.getRepo(),
                githubSchemaProperties.getSchemaPath(),
                githubSchemaProperties.getRef()
        );

        GithubAccessOptions githubAccessOpts = new GithubAccessOptions(
                githubSchemaProperties.getToken()
        );

        GithubNetworkntSchemaProvider githubNetworkntSchemaProvider = new GithubNetworkntSchemaProvider(JsonSchemaType.V2019_09, githubResConfig, githubAccessOpts);

        return new CachingGithubNetworkntSchemaProvider(cacheManager, githubNetworkntSchemaProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchemaMetaService schemaMetaService() {
        return new DefaultSchemaMetaService();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonDynamicModelValidator jsonDynamicModelValidator(NetworkntSchemaProvider provider,
                                                               ObjectMapper mapper, SchemaMetaService schemaMetaService) {
        return new NetworkntJsonDynamicModelValidator(provider, mapper, schemaMetaService);
    }

    @Bean
    @ConditionalOnMissingBean
    public GithubWebhookListenerHttpSchemaServer githubWebhookListenerHttpSchemaServer(GithubSchemaProperties githubSchemaProperties,
                                                                                       Collection<NetworkntCacheManager> cacheManagers) {
        return new GithubWebhookListenerHttpSchemaServer(
                githubSchemaProperties.getWebhookListenerPort(),
                githubSchemaProperties.getWebhookListenerServerContext(),
                cacheManagers
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveDescriptorDeterminator descriptorDeterminator(SchemaMetaService schemaMetaService) {
        return new DefaultReactiveDescriptorDeterminator(schemaMetaService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DateTypesNormalizer dateDocumentTypesNormalizer() {
        return new DefaultDateTypesNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public DatesProcessor datesProcessor() {
        return new DefaultDatesProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonDynamicModelDao jsonDynamicModelDao(ReactiveMongoOperations ops, ReactiveMongoDescriptorFacilities facilities) {
        return new MongoVersionedDynamicModelDao(ops, facilities);
    }
}
