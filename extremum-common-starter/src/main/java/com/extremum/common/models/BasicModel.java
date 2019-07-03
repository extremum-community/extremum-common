package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;

import java.io.Serializable;

public interface BasicModel<ID extends Serializable> extends Model {
    Descriptor getUuid();

    void setUuid(Descriptor uuid);

    ID getId();

    void setId(ID id);

    @Override
    default void copyServiceFieldsTo(Model to) {
        if (!(to instanceof BasicModel)) {
            throw new IllegalStateException("I can only copy to a BasicModel");
        }

        BasicModel<ID> basicTo = (BasicModel<ID>) to;

        basicTo.setId(this.getId());
        basicTo.setUuid(this.getUuid());
    }

    enum FIELDS {
        id, uuid
    }
}
