package com.extremum.common.models;

import com.extremum.common.converters.MongoZonedDateTimeConverter;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Converters({MongoZonedDateTimeConverter.class})
public abstract class MongoCommonModel implements PersistableCommonModel<ObjectId> {
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
    private Boolean deleted = false;


    @PrePersist
    public void fillRequiredFields() {
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
        String name = this.getClass().getAnnotation(ModelName.class).name();
        if (this.uuid == null) {
            this.uuid = MongoDescriptorFactory.create(id, name);
        }
    }
}
