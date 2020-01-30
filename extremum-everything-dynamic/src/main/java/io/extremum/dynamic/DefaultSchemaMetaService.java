package io.extremum.dynamic;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultSchemaMetaService implements SchemaMetaService {
    private final Map<String, String> map = new ConcurrentHashMap<>();

    @Override
    public String getSchemaNameByModel(String modelName) {
        return map.get(modelName);
    }

    @Override
    public void registerMapping(String modelName, String schemaName) {
        map.put(modelName, schemaName);
    }

    @Override
    public Set<String> getModelNames() {
        return ImmutableSet.copyOf(map.keySet());
    }
}
