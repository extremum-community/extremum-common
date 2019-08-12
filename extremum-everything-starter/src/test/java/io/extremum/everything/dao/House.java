package io.extremum.everything.dao;

import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import static io.extremum.everything.dao.House.MODEL_NAME;

/**
 * @author rpuch
 */
@Document(MODEL_NAME)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ModelName(MODEL_NAME)
public class House extends MongoCommonModel {
    public static final String MODEL_NAME = "House";

    private String number;

    public enum FIELDS {
        number
    }
}
