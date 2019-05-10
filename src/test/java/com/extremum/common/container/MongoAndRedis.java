package com.extremum.common.container;

import org.testcontainers.containers.GenericContainer;

/**
 * @author rpuch
 */
public class MongoAndRedis {
    static {
        GenericContainer mongo = new GenericContainer("mongo:3.4-xenial").withExposedPorts(27017);
        mongo.start();
        System.setProperty("mongo.uri",
                "mongodb://" + mongo.getContainerIpAddress() + ":" + mongo.getFirstMappedPort());

        GenericContainer redis = new GenericContainer("redis:5.0.4").withExposedPorts(6379);
        redis.start();
        System.setProperty("redis.uri", "redis://" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort());
    }
}
