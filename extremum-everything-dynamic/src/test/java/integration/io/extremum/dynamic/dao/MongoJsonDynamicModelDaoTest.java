package integration.io.extremum.dynamic.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.TestWithServices;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.dao.MongoJsonDynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.starter.CommonConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ActiveProfiles("save-model-test")
@ContextConfiguration(classes = {CommonConfiguration.class, DynamicModuleAutoConfiguration.class})
@SpringBootTest
class MongoJsonDynamicModelDaoTest extends TestWithServices {
    @Autowired
    MongoJsonDynamicModelDao dao;

    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @BeforeEach
    void beforeEach() {
        when(metadataProvider.provideMetadata(any())).thenReturn(mock(JsonDynamicModel.class));
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