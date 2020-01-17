package io.extremum.dynamic.models.impl;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class JsonDynamicModel implements DynamicModel<Map<String, Object>> {
    private Descriptor id;
    private final String modelName;
    private final Map<String, Object> modelData;
}
