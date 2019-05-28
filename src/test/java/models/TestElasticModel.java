package models;

import com.extremum.common.models.ElasticCommonModel;
import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

import static models.TestMongoModel.COLLECTION;

@ModelName(COLLECTION)
@Getter @Setter
public class TestElasticModel extends ElasticCommonModel {

    public static final String INDEX = "testEntities";

    private String name;

    public enum FIELDS {
        name
    }
}
