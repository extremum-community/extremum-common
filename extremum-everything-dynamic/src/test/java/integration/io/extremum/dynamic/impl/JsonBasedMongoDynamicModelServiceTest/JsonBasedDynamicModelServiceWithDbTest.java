package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.GithubSchemaProperties;
import io.extremum.dynamic.dao.MongoJsonDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.test.containers.MongoContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@ActiveProfiles("save-model-test")
@SpringBootTest(classes = DynamicModuleAutoConfiguration.class)
class JsonBasedDynamicModelServiceWithDbTest {
    @Autowired
    JsonBasedDynamicModelService service;

    @Autowired
    GithubSchemaProperties githubSchemaProperties;

    @Autowired
    NetworkntCacheManager networkntCacheManager;

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
    void validModelSavedInMongo() throws IOException {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        JsonNode modelData = new ObjectMapper().readValue(
                "{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}",
                JsonNode.class
        );

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        Mono<JsonDynamicModel> saved = service.saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .expectNextMatches(m ->
                        m.getModelName().equals(model.getModelName()) &&
                                m.getModelData().toString().equals(model.getModelData().toString()) &&
                                m.getId() != null &&
                                model.getModelName().equals(m.getId().getModelType())
                )
                .verifyComplete();
    }

    @Test
    void getModelById() throws IOException {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        JsonNode modelData = new ObjectMapper().readValue(
                "{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}",
                JsonNode.class
        );

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        JsonDynamicModel saved = service.saveModel(model).block();

        JsonDynamicModel found = dao.getById(saved.getId()).block();

        Assertions.assertNotNull(found);
        Assertions.assertNotNull(found.getId());
        Assertions.assertNotNull(model.getModelName(), found.getId().getModelType());
        Assertions.assertEquals(saved.getId(), found.getId());
        Assertions.assertEquals(model.getModelName(), found.getModelName());
        Assertions.assertEquals(model.getModelData(), found.getModelData());
    }

    @Test
    void sequentialCreationOfModels_createNewCollectionForAModelTest() throws IOException {
        JsonNode data = new ObjectMapper().readValue("{\"a\":\"b\"}", JsonNode.class);

        JsonDynamicModel model = new JsonDynamicModel("Model1", data);

        JsonDynamicModel saved1 = dao.save(model).block();
        JsonDynamicModel saved2 = dao.save(model).block();

        Assertions.assertNotNull(saved1.getId());
        Assertions.assertNotNull(saved2.getId());
        Assertions.assertNotEquals(saved1.getId(), saved2.getId());
    }
}
