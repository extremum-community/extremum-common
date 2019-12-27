package io.extremum.dynamic.models;

import io.extremum.sharedmodels.basic.Model;

public interface DynamicModel<ModelData> extends Model {
    String getModelName();

    ModelData getModelData();
}
