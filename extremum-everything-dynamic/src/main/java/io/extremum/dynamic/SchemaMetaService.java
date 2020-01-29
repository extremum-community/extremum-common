package io.extremum.dynamic;

import java.util.Set;

public interface SchemaMetaService {
    String getSchemaNameByModel(String modelName);

    void registerMapping(String modelName, String schemaName);

    Set<String> getModelNames();
}
