package models;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.HardDelete;
import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import static models.HardDeleteMongoModel.COLLECTION;

@ModelName(COLLECTION)
@Document(COLLECTION)
@Getter @Setter
@HardDelete
public class HardDeleteMongoModel extends MongoCommonModel {

    static final String COLLECTION = "hardDelete";
    private String name;

    public enum FIELDS {
        name
    }
}
