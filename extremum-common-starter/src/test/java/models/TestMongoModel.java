package models;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import static models.TestMongoModel.COLLECTION;

@ModelName(COLLECTION)
@Document(COLLECTION)
@Getter @Setter
public class TestMongoModel extends MongoCommonModel {

    public static final String COLLECTION = "testEntities";
    private String name;

    public enum FIELDS {
        name
    }
}
