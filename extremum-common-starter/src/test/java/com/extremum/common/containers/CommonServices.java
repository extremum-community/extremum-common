package com.extremum.common.containers;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * @author rpuch
 */
public class CommonServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonServices.class);

    static {
        startMongo();
        startRedis();
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

    @NotNull
    private static GenericContainer startGenericContainer(String dockerImageName, int portToExpose) {
        GenericContainer container = new GenericContainer(dockerImageName).withExposedPorts(portToExpose);
        container.start();
        return container;
    }
}
