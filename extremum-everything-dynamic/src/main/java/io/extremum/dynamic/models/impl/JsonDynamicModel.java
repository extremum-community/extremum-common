package io.extremum.dynamic.models.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.extremum.dynamic.models.DynamicModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonDynamicModel implements DynamicModel<JsonNode> {
    private final String modelName;
    private final JsonNode data;

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public JsonNode getModelData() {
        return data;
    }
}
