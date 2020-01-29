package io.extremum.dynamic;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultSchemaMetaService implements SchemaMetaService {
    private final Map<String, String> map = new HashMap<>();

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
