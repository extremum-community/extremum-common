package io.extremum.dynamic.models.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;

@Getter
public class JsonDynamicModel implements DynamicModel<JsonNode> {
    private Descriptor id;
    private final String modelName;
    private final JsonNode modelData;

    public JsonDynamicModel(String modelName, JsonNode data) {
        this.modelName = modelName;
        this.modelData = data;
    }

    public JsonDynamicModel(Descriptor id, String modelName, JsonNode data) {
        this.id = id;
        this.modelName = modelName;
        this.modelData = data;
    }
}
