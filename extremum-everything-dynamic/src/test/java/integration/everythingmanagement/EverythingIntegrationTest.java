package integration.everythingmanagement;

import integration.SpringBootTestWithServices;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.constant.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static io.extremum.dynamic.DynamicModelSupports.collectionNameFromModel;
import static io.extremum.dynamic.utils.DynamicModelTestUtils.buildModel;
import static io.extremum.dynamic.utils.DynamicModelTestUtils.toMap;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureWebTestClient
@SpringBootTest(classes = EverythingIntegrationTestConfiguration.class, webEnvironment = RANDOM_PORT)
public class EverythingIntegrationTest extends SpringBootTestWithServices {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    JsonDynamicModelDao dao;

    @Autowired
    SchemaMetaService schemaMetaService;

    @Test
    void get_returnOkResponse() {
        String modelName = "ADynamicModel";
        schemaMetaService.registerMapping(modelName, "<doesn't matter for this test>", 1);

        JsonDynamicModel model = buildModel(modelName, toMap("{\"field\": \"value\"}"));
        JsonDynamicModel persisted = persistModel(model).block();

        webTestClient.get()
                .uri("/v1/" + persisted.getId().getExternalId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.id").isEqualTo(persisted.getId().getExternalId())
                .jsonPath("$.result.field").isEqualTo("value")
                .jsonPath("$.result.model").isEqualTo(modelName)
                .jsonPath("$.result.field").isEqualTo("value")
                .jsonPath("$.result.version").isEqualTo(1)
                .jsonPath("$.result.created").exists()
                .jsonPath("$.result.modified").exists();
    }

    @Test
    void patch_withValidPatchData_patchingAModel_and_returnOkResponse() {
        String modelName = "ADynamicModelForPatching";
        schemaMetaService.registerMapping(modelName, "simple.schema.json", 1);

        String patch = "[{\"op\":  \"replace\", \"path\": \"/field\", \"value\": \"replaced\"}]";

        JsonDynamicModel model = buildModel(modelName, toMap("{\"field\": \"value\"}"));
        JsonDynamicModel persisted = persistModel(model).block();

        webTestClient.patch()
                .uri("/v1/" + persisted.getId().getExternalId())
                .bodyValue(patch)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.id").isEqualTo(persisted.getId().getExternalId())
                .jsonPath("$.result.field").isEqualTo("replaced")
                .jsonPath("$.result.version", 2).exists()
                .jsonPath("$.result.model").isEqualTo(modelName)
                .jsonPath("$.result.created").exists()
                .jsonPath("$.result.modified").exists();
    }

    @Test
    void patch_withNotValidPatchData_doesnPatchingAModel_and_returnErrorResponse() {
        String modelName = "ADynamicModelForPatchingWithNotValidPatchRequest";
        schemaMetaService.registerMapping(modelName, "simple.schema.json", 1);

        String patch = "[{\"op\":  \"replace\", \"path\": \"/field1\", \"value\": 1}]";

        JsonDynamicModel model = buildModel(modelName, toMap("{\"field1\": \"value\"}"));
        JsonDynamicModel persisted = persistModel(model).block();
        webTestClient.patch()
                .uri("/v1/" + persisted.getId().getExternalId())
                .bodyValue(patch)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("fail")
                .jsonPath("$.code").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.alerts").isArray()
                .jsonPath("$.alerts", hasSize(1));
    }

    @Test
    void removeModel() {
        String modelName = "AModelForRemove";
        schemaMetaService.registerMapping(modelName, "simple.schema.json", 1);

        JsonDynamicModel model = buildModel(modelName, toMap("{\"field\": \"value\"}"));
        JsonDynamicModel persisted = persistModel(model).block();

        webTestClient.delete()
                .uri("/v1/" + persisted.getId().getExternalId())
                .exchange()
                .expectStatus().isOk();

        dao.getByIdFromCollection(persisted.getId(), collectionNameFromModel(modelName));

        webTestClient.get()
                .uri("/v1/" + persisted.getId().getExternalId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private Mono<JsonDynamicModel> persistModel(JsonDynamicModel model) {
        return dao.create(model, collectionNameFromModel(model.getModelName()));
    }
}
