package io.extremum.elasticsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilitiesImpl;
import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilitiesImpl;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.elasticsearch.reactive.ElasticsearchUniversalReactiveModelLoader;
import io.extremum.elasticsearch.springdata.reactiverepository.*;
import io.extremum.elasticsearch.springdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.http.HttpHeaders;

@Configuration
@ConditionalOnProperty("elasticsearch.repository-packages")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@EnableExtremumElasticsearchRepositories(basePackages = "${elasticsearch.repository-packages}",
        repositoryFactoryBeanClass = ExtremumElasticsearchRepositoryFactoryBean.class)
@EnableExtremumReactiveElasticsearchRepositories(basePackages = "${elasticsearch.repository-packages}",
        repositoryFactoryBeanClass = ExtremumReactiveElasticsearchRepositoryFactoryBean.class)
@RequiredArgsConstructor
public class ElasticsearchRepositoriesConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchDescriptorFacilities elasticsearchDescriptorFacilities(DescriptorFactory descriptorFactory,
                                                                               DescriptorSaver descriptorSaver) {
        return new ElasticsearchDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchDescriptorFacilities reactiveElasticsearchDescriptorFacilities(
            ReactiveDescriptorSaver descriptorSaver) {
        return new ReactiveElasticsearchDescriptorFacilitiesImpl(descriptorSaver);
    }

    @Bean
    @ConditionalOnMissingBean
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
    @ConditionalOnMissingBean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient elasticsearchClient,
            ResultsMapper resultsMapper, ElasticsearchDescriptorFacilities elasticsearchDescriptorFactory) {
        return new ExtremumElasticsearchRestTemplate(elasticsearchClient, resultsMapper,
                elasticsearchDescriptorFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        String[] hosts = elasticsearchProperties.getHosts().stream()
                .map(h -> h.getHost() + ":" + h.getPort())
                .toArray(String[]::new);

        return ExtremumReactiveElasticsearchClient.create(HttpHeaders.EMPTY, hosts);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtremumResultMapper resultMapper(ObjectMapper objectMapper) {
        return new ExtremumResultMapper(
                new ExtremumEntityMapper(new SimpleElasticsearchMappingContext(), objectMapper)
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchOperations reactiveElasticsearchTemplate(
            ReactiveElasticsearchClient reactiveElasticsearchClient, ObjectMapper objectMapper,
            ReactiveElasticsearchDescriptorFacilities descriptorFacilities) {
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        return new ExtremumReactiveElasticsearchTemplate(reactiveElasticsearchClient,
                new MappingElasticsearchConverter(mappingContext),
                resultMapper(objectMapper),
                descriptorFacilities);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveElasticsearchAdditionalOperations reactiveElasticsearchAdditionalOperations(
            ReactiveElasticsearchClient reactiveElasticsearchClient,
            ElasticsearchOperations elasticsearchOperations,
            ResultsMapper resultsMapper) {
        return new ReactiveElasticsearchAdditionalOperationsImpl(reactiveElasticsearchClient, resultsMapper,
                elasticsearchOperations);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchUniversalReactiveModelLoader elasticsearchUniversalReactiveModelLoader(
            ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        return new ElasticsearchUniversalReactiveModelLoader(reactiveElasticsearchOperations);
    }
}
