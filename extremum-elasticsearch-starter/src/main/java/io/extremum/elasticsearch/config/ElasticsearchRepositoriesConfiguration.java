package io.extremum.elasticsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilitiesImpl;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.elasticsearch.reactive.ElasticsearchUniversalReactiveModelLoader;
import io.extremum.elasticsearch.repositories.*;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.DefaultReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.http.HttpHeaders;

@Configuration
@ConditionalOnProperty("elasticsearch.repository-packages")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@EnableExtremumElasticsearchRepositories(basePackages = "${elasticsearch.repository-packages}",
        repositoryFactoryBeanClass = ExtremumElasticsearchRepositoryFactoryBean.class)
@RequiredArgsConstructor
public class ElasticsearchRepositoriesConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    @Bean
    public ElasticsearchDescriptorFacilities elasticsearchDescriptorFacilities(DescriptorFactory descriptorFactory,
                                                                               DescriptorSaver descriptorSaver) {
        return new ElasticsearchDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver);
    }

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        HttpHost[] httpHosts = elasticsearchProperties.getHosts().stream()
                .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                .toArray(HttpHost[]::new);
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        if (elasticsearchProperties.getUsername() != null && elasticsearchProperties.getPassword() != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword()));
            restClientBuilder.setHttpClientConfigCallback(clientBuilder -> clientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider));
        }
        return new RestHighLevelClient(restClientBuilder);
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient elasticsearchClient,
                                                         ObjectMapper objectMapper, ElasticsearchDescriptorFacilities elasticsearchDescriptorFactory) {
        return new ExtremumElasticsearchRestTemplate(elasticsearchClient, objectMapper,
                elasticsearchDescriptorFactory);
    }

    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        String[] hosts = elasticsearchProperties.getHosts().stream()
                .map(h -> h.getHost() + ":" + h.getPort())
                .toArray(String[]::new);

        return DefaultReactiveElasticsearchClient.create(HttpHeaders.EMPTY, hosts);
    }

    @Bean
    public ReactiveElasticsearchOperations reactiveElasticsearchOperations(
            ReactiveElasticsearchClient reactiveElasticsearchClient, ObjectMapper objectMapper) {
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        return new ReactiveElasticsearchTemplate(reactiveElasticsearchClient,
                new MappingElasticsearchConverter(mappingContext),
                new ExtremumResultMapper(
                        new ExtremumEntityMapper(new SimpleElasticsearchMappingContext(), objectMapper)
                ));
    }

    @Bean
    public ElasticsearchUniversalReactiveModelLoader elasticsearchUniversalReactiveModelLoader(
            ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        return new ElasticsearchUniversalReactiveModelLoader(reactiveElasticsearchOperations);
    }
}
