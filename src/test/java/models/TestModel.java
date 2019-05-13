package models;

import com.extremum.common.models.MongoCommonModel;
import org.springframework.data.mongodb.core.mapping.Document;

import static models.TestModel.COLLECTION;

@Document(COLLECTION)
public class TestModel extends MongoCommonModel {

    static final String COLLECTION = "testEntities";

    public String name;

    @Override
    public String getModelName() {
        return COLLECTION;
    }

    public enum FIELDS {
        name
    }
}
