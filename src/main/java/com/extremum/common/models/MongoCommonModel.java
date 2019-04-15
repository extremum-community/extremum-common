package com.extremum.common.models;

import com.extremum.common.converters.ZonedDateTimeConverter;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Setter
@Converters({ZonedDateTimeConverter.class})
public abstract class MongoCommonModel extends AbstractCommonModel<ObjectId> {

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
}
