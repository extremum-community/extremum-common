package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.extremum.elasticsearch.repositories.ExtremumElasticsearchRepository;
import com.extremum.starter.CommonConfiguration;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
    public Client elasticsearchClient() throws UnknownHostException {
        Settings elasticsearchSettings = Settings.builder()
//                .put("client.transport.sniff", true)
//                .put("path.home", elasticsearchHome)
//                .put("cluster.name", clusterName)
                .build();
        TransportClient client = new PreBuiltTransportClient(elasticsearchSettings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        return client;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws UnknownHostException {
//        return new ElasticsearchTemplate(nodeBuilder().local(true).node().client());
        return new ElasticsearchTemplate(elasticsearchClient());
    }
}
