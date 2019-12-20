package integration.io.extremum.dynamic.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.dao.MongoJsonDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.test.containers.MongoContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;

@Slf4j
@ActiveProfiles("save-model-test")
@SpringBootTest(classes = DynamicModuleAutoConfiguration.class)
class MongoJsonDynamicModelDaoTest {
    @Autowired
    MongoJsonDynamicModelDao dao;

    static {
        new MongoContainer();

        GenericContainer redis = new GenericContainer("redis:5.0.4")
                .withExposedPorts(6379);
        redis.start();

        String redisUri = String.format("redis://%s:%d", redis.getContainerIpAddress(), redis.getFirstMappedPort());
        System.setProperty("redis.uri", redisUri);

        log.info("Redis uri is {}", redisUri);
    }

    @Test
    void sequentialCreationOfModels_createNewCollectionForAModelTest() throws IOException {
        JsonNode data = new ObjectMapper().readValue("{\"a\":\"b\"}", JsonNode.class);

        JsonDynamicModel model = new JsonDynamicModel("Model1", data);

        String collectionName = "model1";

        JsonDynamicModel saved1 = dao.save(model, collectionName).block();
        JsonDynamicModel saved2 = dao.save(model, collectionName).block();

        Assertions.assertNotNull(saved1.getId());
        Assertions.assertNotNull(saved2.getId());
        Assertions.assertNotEquals(saved1.getId(), saved2.getId());
    }
}