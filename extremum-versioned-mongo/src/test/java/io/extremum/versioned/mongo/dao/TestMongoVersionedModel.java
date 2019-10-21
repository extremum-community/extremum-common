package io.extremum.versioned.mongo.dao;

import io.extremum.common.model.annotation.ModelName;
import io.extremum.versioned.mongo.model.MongoVersionedModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("versionedModel")
@ModelName("TestMongoVersionedModel")
@Getter
@Setter
@ToString
public class TestMongoVersionedModel extends MongoVersionedModel {
    private String name;
}
