package io.extremum.dynamic.models.impl;

import io.extremum.common.model.annotation.ModelName;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@AllArgsConstructor
@ModelName(DynamicModel.MODEL_TYPE)
@EqualsAndHashCode(callSuper = true)
public class JsonDynamicModel extends MongoCommonModel implements DynamicModel<Map<String, Object>> {
    // the schema version to set, if schema version was not specified at constructor
    private final static int DEFAULT_SCHEMA_VERSION = 1;

    public JsonDynamicModel(Descriptor id, String modelName, Map<String, Object> modelData) {
        super.setUuid(id);
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = modelName;
        this.schemaVersion = DEFAULT_SCHEMA_VERSION;
    }

    public JsonDynamicModel(String modelName, Map<String, Object> modelData) {
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = modelName;
        this.schemaVersion = DEFAULT_SCHEMA_VERSION;
    }

    public JsonDynamicModel(String modelName, Map<String, Object> modelData, String schemaName, Integer schemaVersion) {
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
    }

    public JsonDynamicModel(Descriptor id, String modelName, Map<String, Object> modelData, String schemaName, Integer schemaVersion) {
        super.setUuid(id);
        this.modelName = modelName;
        this.modelData = modelData;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
    }

    private final String modelName;
    private final Map<String, Object> modelData;
    private final String schemaName;
    private final int schemaVersion;
}