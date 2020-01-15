package io.extremum.dynamic.models.impl;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BsonDynamicModel implements DynamicModel<Document> {
    private Descriptor id;
    private final String modelName;
    private final Document modelData;
}
