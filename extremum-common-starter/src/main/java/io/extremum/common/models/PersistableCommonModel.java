package io.extremum.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface PersistableCommonModel<ID extends Serializable> extends BasicModel<ID> {
    ZonedDateTime getCreated();

    void setCreated(ZonedDateTime created);

    ZonedDateTime getModified();

    void setModified(ZonedDateTime modified);

    Long getVersion();

    void setVersion(Long version);

    Boolean getDeleted();

    void setDeleted(Boolean deleted);

    @Override
    default void copyServiceFieldsTo(Model to) {
        if (!(to instanceof PersistableCommonModel)) {
            throw new IllegalStateException("I can only copy to a PersistableCommonModel");
        }

        PersistableCommonModel<ID> persistableTo = (PersistableCommonModel<ID>) to;

        BasicModel.super.copyServiceFieldsTo(persistableTo);

        persistableTo.setVersion(this.getVersion());
        persistableTo.setDeleted(this.getDeleted());
        persistableTo.setCreated(this.getCreated());
        persistableTo.setModified(this.getModified());
    }

    @JsonIgnore
    default boolean isNotDeleted() {
        return getDeleted() == null || !getDeleted();
    }

    enum FIELDS {
        id, uuid, created, modified, version, deleted
    }
}
