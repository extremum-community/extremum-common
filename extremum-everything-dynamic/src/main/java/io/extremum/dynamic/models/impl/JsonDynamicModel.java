package io.extremum.dynamic.models.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.common.model.BasicModel;
import io.extremum.common.model.annotation.ModelName;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@Getter
@ModelName(DynamicModel.MODEL_TYPE)
@AllArgsConstructor
@RequiredArgsConstructor
public class JsonDynamicModel implements DynamicModel<Map<String, Object>>, BasicModel<Descriptor> {
    private Descriptor id;
    private final String modelName;
    private final Map<String, Object> modelData;

    @JsonIgnore
    @Override
    public Descriptor getUuid() {
        return id;
    }

    @JsonIgnore
    @Override
    public void setUuid(Descriptor uuid) {
        throw new UnsupportedOperationException();
    }
}
