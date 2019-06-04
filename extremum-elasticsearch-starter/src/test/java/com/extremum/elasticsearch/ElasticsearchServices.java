package com.extremum.elasticsearch;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * @author rpuch
 */
class ElasticsearchServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServices.class);

    static {
        startMongo();
        startRedis();
        startElasticsearch();
    }

    private static void startMongo() {
        GenericContainer mongo = startGenericContainer("mongo:3.4-xenial", 27017);
        String mongoUri = "mongodb://" + mongo.getContainerIpAddress() + ":" + mongo.getFirstMappedPort();
        System.setProperty("mongo.uri", mongoUri);
        LOGGER.info("MongoDB uri is {}", mongoUri);
    }

    private static void startRedis() {
        GenericContainer redis = startGenericContainer("redis:5.0.4", 6379);
        String redisUri = String.format("redis://%s:%d", redis.getContainerIpAddress(), redis.getFirstMappedPort());
        System.setProperty("redis.uri", redisUri);
        LOGGER.info("Redis uri is {}", redisUri);
    }

    private static void startElasticsearch() {
        if ("true".equals(System.getProperty("start.elasticsearch", "true"))) {
            ElasticsearchContainer elasticSearch = new ElasticsearchContainer("elasticsearch:7.1.0");
            elasticSearch.start();

            System.setProperty("elasticsearch.hosts[0].host", elasticSearch.getContainerIpAddress());
            System.setProperty("elasticsearch.hosts[0].port", Integer.toString(elasticSearch.getFirstMappedPort()));
            System.setProperty("elasticsearch.hosts[0].protocol", "http");

            LOGGER.info("Elasticsearch host:port are {}:{}",
                    elasticSearch.getContainerIpAddress(), elasticSearch.getFirstMappedPort());
        } else {
            System.setProperty("elasticsearch.hosts[0].host", "localhost");
            System.setProperty("elasticsearch.hosts[0].port", "9200");
            System.setProperty("elasticsearch.hosts[0].protocol", "http");
        }
    }

    @NotNull
    private static GenericContainer startGenericContainer(String dockerImageName, int portToExpose) {
        GenericContainer container = new GenericContainer(dockerImageName).withExposedPorts(portToExpose);
        container.start();
        return container;
    }
}
