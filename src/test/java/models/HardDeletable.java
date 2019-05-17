package models;

import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "hard_deletable")
@Setter
@ModelName("HardDeletable")
public class HardDeletable extends PostgresCommonModel {

    @Getter(onMethod_ = {@Column})
    private String name;

    public enum FIELDS {
        name
    }
}
