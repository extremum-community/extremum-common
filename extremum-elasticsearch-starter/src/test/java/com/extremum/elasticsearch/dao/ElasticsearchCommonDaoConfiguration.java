package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.extremum.elasticsearch.repositories.ExtremumElasticsearchRepository;
import com.extremum.elasticsearch.repositories.ExtremumElasticsearchRestTemplate;
import com.extremum.starter.CommonConfiguration;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
@Import(CommonConfiguration.class)
@EnableElasticsearchRepositories(basePackages = "com.extremum.elasticsearch.dao",
        repositoryBaseClass = ExtremumElasticsearchRepository.class)
@RequiredArgsConstructor
public class ElasticsearchCommonDaoConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    @Bean
    public ElasticsearchDescriptorFactory elasticDescriptorFactory() {
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
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ExtremumElasticsearchRestTemplate(elasticsearchClient());
    }
}
