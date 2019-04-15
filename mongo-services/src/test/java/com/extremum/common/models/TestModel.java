package com.extremum.common.models;

import org.mongodb.morphia.annotations.Entity;

import static com.extremum.common.models.TestModel.COLLECTION;

@Entity(COLLECTION)
public class TestModel extends MongoCommonModel {

    static final String COLLECTION = "testEntities";

    @Override
    public String getModelName() {
        return COLLECTION;
    }

}
