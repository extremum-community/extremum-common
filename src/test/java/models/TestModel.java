package models;

import com.extremum.common.models.MongoCommonModel;
import org.mongodb.morphia.annotations.Entity;

import static models.TestModel.COLLECTION;

@Entity(COLLECTION)
public class TestModel extends MongoCommonModel {

    static final String COLLECTION = "testEntities";

    @Override
    public String getModelName() {
        return COLLECTION;
    }
}
