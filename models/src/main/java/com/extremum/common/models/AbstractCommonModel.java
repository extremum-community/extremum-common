package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @author vov4a on 07.08.16
 */
public abstract class AbstractCommonModel<ID extends Serializable> implements PersistableCommonModel<ID> {
    public static final long VERSION_INITIAL_VALUE = 0;
    public static final boolean DELETED_INITIAL_VALUE = false;

    @JsonIgnore
    @Override
    public abstract String getModelName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractCommonModel that = (AbstractCommonModel) o;

        return (Objects.equals(getId(), that.getId())) &&
                (Objects.equals(getVersion(), that.getVersion()));
    }

    @Override
    public int hashCode() {
        int result = getVersion() != null ? getVersion().hashCode() : 0;
        result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "<" + this.getClass().getSimpleName() + " : [" +
                FIELDS.id.name() + "(" +
                    "internal: " + ofNullable(getId()).map(Object::toString).orElse("<definition not available>") + ", " +
                    "external: " + ofNullable(getUuid()).map(Descriptor::getExternalId).orElse("<definition not available>") + "), " +
                FIELDS.created.name() + ": " + ofNullable(this.getCreated()).map(DateUtils::convert).orElse("<definition not available>") + ", " +
                FIELDS.modified.name() + ": " + ofNullable(this.getModified()).map(DateUtils::convert).orElse("<definition not available>") + ", " +
                FIELDS.deleted.name() + ": " + this.getDeleted() +
                "] >";
    }

    public enum FIELDS {
        id, uuid, created, modified, version, deleted
    }
}
