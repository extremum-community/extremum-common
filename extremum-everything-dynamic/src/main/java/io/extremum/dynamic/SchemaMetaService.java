package io.extremum.dynamic;

import java.util.Set;

public interface SchemaMetaService {
    String getSchemaNameByNameAndVersion(String modelName);

    String getSchemaNameByNameAndVersion(String modelName, Integer schemaVersion);

    void registerMapping(String modelName, String schemaName);

    void registerMapping(String modelName, String schemaName, Integer schemaVersion);

    Set<String> getModelNames();
}
