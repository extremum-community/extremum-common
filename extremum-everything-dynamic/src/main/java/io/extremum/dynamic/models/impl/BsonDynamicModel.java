package io.extremum.dynamic.models.impl;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;

@Data
@Getter
public class BsonDynamicModel implements DynamicModel<Document> {
    private Descriptor id;
    private final String modelName;
    private final Document modelData;

    public BsonDynamicModel(String modelName, Document data) {
        this.modelName = modelName;
        this.modelData = data;
    }

    public BsonDynamicModel(Descriptor id, String modelName, Document data) {
        this.id = id;
        this.modelName = modelName;
        this.modelData = data;
    }
}
