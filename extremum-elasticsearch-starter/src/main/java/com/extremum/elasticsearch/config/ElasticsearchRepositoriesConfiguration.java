package com.extremum.elasticsearch.config;

import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.extremum.elasticsearch.repositories.EnableExtremumElasticsearchRepositories;
import com.extremum.elasticsearch.repositories.ExtremumElasticsearchRepositoryFactoryBean;
import com.extremum.elasticsearch.repositories.ExtremumElasticsearchRestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty("elasticsearch.repository-packages")
@EnableConfigurationProperties(ElasticsearchProperties.class)
@EnableExtremumElasticsearchRepositories(basePackages = "${elasticsearch.repository-packages}",
        repositoryFactoryBeanClass = ExtremumElasticsearchRepositoryFactoryBean.class)
@RequiredArgsConstructor
public class ElasticsearchRepositoriesConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    @Bean
    public ElasticsearchDescriptorFactory elasticsearchDescriptorFactory() {
        return new ElasticsearchDescriptorFactory();
    }

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        List<HttpHost> httpHosts = elasticsearchProperties.getHosts().stream()
                .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                .collect(Collectors.toList());

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));

        return new RestHighLevelClient(builder);
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(ObjectMapper objectMapper) {
        return new ExtremumElasticsearchRestTemplate(elasticsearchClient(), objectMapper,
                elasticsearchDescriptorFactory());
    }
}
