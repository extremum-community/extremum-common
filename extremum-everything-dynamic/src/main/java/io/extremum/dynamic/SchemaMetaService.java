package io.extremum.dynamic;

import java.util.Set;

public interface SchemaMetaService {

    String getSchema(String modelName, int schemaVersion);

    void registerMapping(String modelName, String schemaName, int schemaVersion);

    Set<String> getModelNames();
}
