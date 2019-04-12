package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface PersistableCommonModel<ID extends Serializable> extends Model {
    Descriptor getUuid();

    void setUuid(Descriptor uuid);

    ID getId();

    void setId(ID id);

    ZonedDateTime getCreated();

    void setCreated(ZonedDateTime created);

    ZonedDateTime getModified();

    void setModified(ZonedDateTime modified);

    Long getVersion();

    void setVersion(Long version);

    Boolean getDeleted();

    void setDeleted(Boolean deleted);
}
