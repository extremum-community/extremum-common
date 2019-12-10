package io.extremum.dynamic.models.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonBasedDynamicModelTest {
    @Test
    void JsonBasedDynamicModel() throws IOException {
        String modelName = UUID.randomUUID().toString();
        JsonNode node = new ObjectMapper().readValue("{\"a\":\"b\"}", JsonNode.class);

        JsonBasedDynamicModel model = new JsonBasedDynamicModel(modelName, node);

        assertEquals(modelName, model.getModelName());
        assertEquals(node.toString(), model.getModelData().toString());
    }
}