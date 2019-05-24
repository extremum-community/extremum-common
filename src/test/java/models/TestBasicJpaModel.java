package models;

import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "test_non_standard_model")
@Setter
@ModelName("TestNonStandardModel")
public class TestBasicJpaModel extends PostgresBasicModel {

    @Getter
    private String name;

    public enum FIELDS {
        name
    }
}
