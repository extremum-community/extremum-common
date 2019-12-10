package io.extremum.dynamic.models;

public interface DynamicModel<ModelData> {
    String getModelName();

    ModelData getModelData();
}
