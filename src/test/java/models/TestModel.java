package models;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import org.mongodb.morphia.annotations.Entity;

import static models.TestModel.COLLECTION;

@ModelName(name = COLLECTION)
@Entity(COLLECTION)
public class TestModel extends MongoCommonModel {
    static final String COLLECTION = "testEntities";
}
