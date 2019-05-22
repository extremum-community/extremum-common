package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;

import java.io.Serializable;

public interface BasicModel<ID extends Serializable> extends Model {
    Descriptor getUuid();

    void setUuid(Descriptor uuid);

    ID getId();

    void setId(ID id);

    enum FIELDS {
        id, uuid
    }
}
