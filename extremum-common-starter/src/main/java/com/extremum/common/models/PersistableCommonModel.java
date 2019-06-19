package com.extremum.common.models;

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

    default <SELF extends PersistableCommonModel<ID>> void copyServiceFieldsTo(SELF to) {
        BasicModel.super.copyServiceFieldsTo(to);

        to.setVersion(this.getVersion());
        to.setDeleted(this.getDeleted());
        to.setCreated(this.getCreated());
        to.setModified(this.getModified());
    }

    default boolean isNotDeleted() {
        return getDeleted() == null || !getDeleted();
    }

    enum FIELDS {
        id, uuid, created, modified, version, deleted
    }
}
