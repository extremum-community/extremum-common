package integration;

import io.extremum.test.containers.MongoContainer;
import org.testcontainers.containers.GenericContainer;

public class TestWithServices {
    static {
        new MongoContainer();

        GenericContainer redis = new GenericContainer("redis:5.0.4")
                .withExposedPorts(6379);
        redis.start();

        String redisUri = String.format("redis://%s:%d", redis.getContainerIpAddress(), redis.getFirstMappedPort());
        System.setProperty("redis.uri", redisUri);
    }
}
