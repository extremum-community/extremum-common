package models;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import org.springframework.data.mongodb.core.mapping.Document;

import static models.TestModel.COLLECTION;

@ModelName(COLLECTION)
@Document(COLLECTION)
public class TestModel extends MongoCommonModel {
    static final String COLLECTION = "testEntities";
    public String name;

    public enum FIELDS {
        name
    }
}
