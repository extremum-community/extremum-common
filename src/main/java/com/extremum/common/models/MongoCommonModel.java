package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class MongoCommonModel implements PersistableCommonModel<ObjectId> {
    @Transient
    private Descriptor uuid;

    @Id
    private ObjectId id;

//    @CreatedDate
    private ZonedDateTime created;

    @LastModifiedDate
    private ZonedDateTime modified;

    @Version
    private Long version;

    private Boolean deleted = false;


    public void fillRequiredFields() {
        initCreated();
//        initModified();

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


    public void resolveDescriptor() {
        this.uuid = MongoDescriptorFactory.fromInternalId(id);
    }

    public void createDescriptorIfNeeded() {
        if (this.uuid == null) {
            this.uuid = MongoDescriptorFactory.create(id, getModelName());
        }
    }
}
