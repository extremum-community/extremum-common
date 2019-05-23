package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;

import java.io.Serializable;

public interface BasicModel<ID extends Serializable> extends Model {
    Descriptor getUuid();

    void setUuid(Descriptor uuid);

    ID getId();

    void setId(ID id);

    default <SELF extends BasicModel<ID>> void copyServiceFieldsTo(SELF to) {
        to.setUuid(this.getUuid());
    }

    enum FIELDS {
        id, uuid
    }
}
