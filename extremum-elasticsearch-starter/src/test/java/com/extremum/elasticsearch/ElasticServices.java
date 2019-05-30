package com.extremum.elasticsearch;

import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * @author rpuch
 */
class ElasticServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticServices.class);

    static {
        startMongo();
        startRedis();
        startElasticSearch();
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

    private static void startElasticSearch() {
        ElasticsearchContainer elasticSearch = new ElasticsearchContainer("7.1.0");
        elasticSearch.start();

        System.setProperty("elastic.hosts[0].host", elasticSearch.getContainerIpAddress());
        System.setProperty("elastic.hosts[0].port", Integer.toString(elasticSearch.getFirstMappedPort()));
        System.setProperty("elastic.hosts[0].protocol", "http");

        LOGGER.info("Elasticsearch host:port are {}:{}",
                elasticSearch.getContainerIpAddress(), elasticSearch.getFirstMappedPort());
    }

    @NotNull
    private static GenericContainer startGenericContainer(String dockerImageName, int portToExpose) {
        GenericContainer container = new GenericContainer(dockerImageName).withExposedPorts(portToExpose);
        container.start();
        return container;
    }
}
