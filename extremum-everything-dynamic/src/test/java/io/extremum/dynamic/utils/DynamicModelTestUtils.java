package io.extremum.dynamic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import lombok.SneakyThrows;

import java.util.Map;

public class DynamicModelTestUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static Map<String, Object> toMap(String value) {
        return mapper.readerFor(Map.class).readValue(value);
    }

    public static String modelNameToCollectionName(String modelName) {
        return modelName.toLowerCase().replaceAll("[\\W]", "_");
    }

    public static JsonDynamicModel buildModel(String modelName, Map<String, Object> data) {
        return new JsonDynamicModel(modelName, data);
    }
}
