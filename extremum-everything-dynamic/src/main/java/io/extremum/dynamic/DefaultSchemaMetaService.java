package io.extremum.dynamic;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSchemaMetaService implements SchemaMetaService {
    private final Map<String, String> map = new ConcurrentHashMap<>();
    private final Map<SchemaKey, String> versionedMap = new ConcurrentHashMap<>();

    @Override
    public String getSchemaNameByNameAndVersion(String modelName) {
        return map.get(modelName);
    }

    @Override
    public String getSchemaNameByNameAndVersion(String modelName, Integer schemaVersion) {
        if (schemaVersion == null) {
            return getSchemaNameByNameAndVersion(modelName);
        } else {
            return versionedMap.get(new SchemaKey(modelName, schemaVersion));
        }
    }

    @Override
    public void registerMapping(String modelName, String schemaName) {
        map.put(modelName, schemaName);
    }

    @Override
    public void registerMapping(String modelName, String schemaName, Integer schemaVersion) {
        if (schemaVersion == null) {
            registerMapping(modelName, schemaName);
        } else {
            versionedMap.put(new SchemaKey(modelName, schemaVersion), schemaName);
        }
    }

    @Override
    public Set<String> getModelNames() {
        return ImmutableSet.copyOf(map.keySet());
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    static class SchemaKey {
        String schemaName;
        Integer schemaVersion;
    }
}
