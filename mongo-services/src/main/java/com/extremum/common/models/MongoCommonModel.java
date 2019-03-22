package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.time.ZonedDateTime;

/**
 * @author vov4a on 07.08.16
 */
@Data
public abstract class MongoCommonModel implements Model {

    public static final int VERSION_INITIAL_VALUE = 0;
    public static final boolean DELETED_INITIAL_VALUE = false;

    @Transient
    public Descriptor uuid;

    @Id
    public ObjectId id;

    @Property
    public ZonedDateTime created;

    @Property
    public ZonedDateTime modified;

    @Property
    @Version
    public Long version;

    @Property
    public Boolean deleted;



    @PrePersist
    public void resolveId() {
        initCreated();
        initModified();

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


    public enum FIELDS {
        id, created, modified, version, deleted
    }
}
