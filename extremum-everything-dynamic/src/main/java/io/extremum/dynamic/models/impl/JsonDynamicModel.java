package io.extremum.dynamic.models.impl;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
@Getter
public class JsonDynamicModel implements DynamicModel<Map<String, Object>> {
    private Descriptor id;
    private final String modelName;
    private final Map<String, Object> modelData;

    public JsonDynamicModel(String modelName, Map<String, Object> data) {
        this.modelName = modelName;
        this.modelData = data;
    }

    public JsonDynamicModel(Descriptor id, String modelName, Map<String, Object> data) {
        this.id = id;
        this.modelName = modelName;
        this.modelData = data;
    }
}
