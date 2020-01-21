package io.extremum.dynamic.watch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.processor.WatchEventConsumer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultDynamicModelWatchServiceTest {
    private static final String MODEL_NAME = "modelName";
    private static final Descriptor DESCRIPTOR = Descriptor.builder()
            .internalId("internal")
            .externalId("external")
            .storageType(Descriptor.StorageType.MONGO)
            .modelType(MODEL_NAME)
            .build();

    static ObjectMapper mapper = new ObjectMapper();

    WatchEventConsumer watchEventConsumer;

    DynamicModelWatchService watchService;

    private Map<String, Object> modelData = new HashMap<String, Object>() {{
        put("a", "b");
    }};

    @BeforeEach
    void beforeEach() {
        watchEventConsumer = mock(WatchEventConsumer.class);
        watchService = new DefaultDynamicModelWatchService(watchEventConsumer, mapper);
    }

    @Test
    void watchPatchOperation() throws JSONException, IOException {
        JsonDynamicModel model = new JsonDynamicModel(DESCRIPTOR, MODEL_NAME, modelData);

        JsonNode node = mapper.readValue("[{\"op\":\"add\",\"path\":\"/tags/-\",\"value\": \"v\"}]", JsonNode.class);

        JsonPatch patch = JsonPatch.fromJson(node);
        watchService.watchPatchOperation(patch, model).block();

        ArgumentCaptor<TextWatchEvent> captor = ArgumentCaptor.forClass(TextWatchEvent.class);

        verify(watchEventConsumer).consume(captor.capture());

        TextWatchEvent captured = captor.getValue();

        String jsonPatch = captured.getJsonPatch();

        JSONArray jsonArray = new JSONArray(jsonPatch);
        JSONObject json = jsonArray.getJSONObject(0);
        String value = json.getString("value");

        assertEquals("v", value);
        assertEquals("add", json.getString("op"));
        assertEquals("/tags/-", json.getString("path"));

        assertEquals(DESCRIPTOR.getInternalId(), captured.getModelId());
    }

    @Test
    void watchSaveOperation() throws JSONException {
        JsonDynamicModel model = new JsonDynamicModel(DESCRIPTOR, MODEL_NAME, modelData);

        watchService.watchSaveOperation(model).block();

        ArgumentCaptor<TextWatchEvent> captor = ArgumentCaptor.forClass(TextWatchEvent.class);

        verify(watchEventConsumer).consume(captor.capture());

        TextWatchEvent captured = captor.getValue();

        String jsonPatch = captured.getJsonPatch();

        JSONArray jsonArray = new JSONArray(jsonPatch);
        JSONObject json = jsonArray.getJSONObject(0);
        JSONObject valueObject = json.getJSONObject("value");

        assertNotNull(valueObject);
        assertEquals("replace", json.getString("op"));
        assertEquals("/", json.getString("path"));

        assertEquals(DESCRIPTOR.getInternalId(), captured.getModelId());
    }

    @Test
    void watchDeleteOperation() throws JSONException {
        JsonDynamicModel model = new JsonDynamicModel(DESCRIPTOR, MODEL_NAME, modelData);

        watchService.watchDeleteOperation(model).block();

        ArgumentCaptor<TextWatchEvent> captor = ArgumentCaptor.forClass(TextWatchEvent.class);

        verify(watchEventConsumer).consume(captor.capture());

        TextWatchEvent captured = captor.getValue();

        String jsonPatch = captured.getJsonPatch();

        JSONArray jsonArray = new JSONArray(jsonPatch);
        JSONObject json = jsonArray.getJSONObject(0);

        assertEquals("remove", json.getString("op"));
        assertEquals("/", json.getString("path"));

        assertEquals(DESCRIPTOR.getInternalId(), captured.getModelId());
    }
}
