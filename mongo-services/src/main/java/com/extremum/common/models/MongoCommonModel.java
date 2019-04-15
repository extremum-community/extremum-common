package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.time.ZonedDateTime;
import java.util.Objects;

import static java.util.Optional.ofNullable;


@Getter
@Setter
public abstract class MongoCommonModel implements Model {

    public static final int VERSION_INITIAL_VALUE = 0;
    public static final boolean DELETED_INITIAL_VALUE = false;

    @Transient
    private Descriptor uuid;

    @Id
    private ObjectId id;

    @Property
    private ZonedDateTime created;

    @Property
    private ZonedDateTime modified;

    @Property
    @Version
    private Long version;

    @Property
    private Boolean deleted;


    @PrePersist
    public void fillRequiredFields() {
        initCreated();
        initModified();
        initDeleted();
        initVersion();

        if (this.id == null && this.uuid != null) {
            this.id = MongoDescriptorFactory.resolve(uuid);
        }
    }

    private void initCreated() {
        if (this.id == null && this.created == null) {
            this.created = ZonedDateTime.now();
        }
    }

    private void initModified() {
        this.modified = ZonedDateTime.now();
    }

    private void initVersion() {
        if (this.id == null && this.version == null) {
            this.version = 0L;
        }
    }

    private void initDeleted() {
        if (this.id == null && this.deleted == null) {
            this.deleted = false;
        }
    }

    @PostLoad
    public void resolveDescriptor() {
        this.uuid = MongoDescriptorFactory.fromInternalId(id);
    }

    @PostPersist
    public void createDescriptorIfNeeded() {
        if (this.uuid == null) {
            this.uuid = MongoDescriptorFactory.create(id, getModelName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MongoCommonModel that = (MongoCommonModel) o;

        return (Objects.equals(id, that.id)) && (Objects.equals(version, that.version));
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
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


    // TODO по deleted искать нельзя: ничего не вернется. Должен ли он быть в public доступе?
    public enum FIELDS {
        id, created, modified, version, deleted
    }
}
