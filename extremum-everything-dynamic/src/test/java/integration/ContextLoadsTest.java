package integration;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.starter.CommonConfiguration;
import io.extremum.test.containers.MongoContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;

@ActiveProfiles("load-context-test")
@ContextConfiguration(classes = {CommonConfiguration.class, DynamicModuleAutoConfiguration.class})
@SpringBootTest
public class ContextLoadsTest {
    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    static {
        new MongoContainer();

        GenericContainer redis = new GenericContainer("redis:5.0.4")
                .withExposedPorts(6379);
        redis.start();

        String redisUri = String.format("redis://%s:%d", redis.getContainerIpAddress(), redis.getFirstMappedPort());
        System.setProperty("redis.uri", redisUri);
    }

    @Test
    void contextLoads() {

    }
}
