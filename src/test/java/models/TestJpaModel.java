package models;

import com.extremum.common.models.PostgresCommonModel;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;

import static models.TestMongoModel.COLLECTION;

@Entity
@Table(name = "test_model")
@Where(clause = "deleted = false")
public class TestJpaModel extends PostgresCommonModel {

    public String name;

    @Override
    public String getModelName() {
        return COLLECTION;
    }

    public enum FIELDS {
        name
    }
}
