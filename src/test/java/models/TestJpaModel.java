package models;

import com.extremum.common.models.PostgresCommonModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import static models.TestMongoModel.COLLECTION;

@Entity
@Table(name = "test_model")
@Where(clause = "deleted = false")
@Setter
public class TestJpaModel extends PostgresCommonModel {

    @Getter(onMethod_ = {@Column(name = "name")})
    private String name;

    @Override
    @Transient
    public String getModelName() {
        return COLLECTION;
    }

    public enum FIELDS {
        name
    }
}
