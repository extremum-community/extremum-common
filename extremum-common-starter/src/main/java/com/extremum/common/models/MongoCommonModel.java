package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.utils.ModelUtils;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.*;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class MongoCommonModel implements PersistableCommonModel<ObjectId> {
    @Transient
    private Descriptor uuid;

    @Id
    private ObjectId id;

    @CreatedDate
    private ZonedDateTime created;

    @LastModifiedDate
    private ZonedDateTime modified;

    @Version
    private Long version;

    private Boolean deleted = false;


    public void fillRequiredFields() {
        if (this.id == null && this.uuid != null) {
            this.id = MongoDescriptorFactory.resolve(uuid);
        }
    }

    public void resolveDescriptor() {
        this.uuid = MongoDescriptorFactory.fromInternalId(id);
    }

    public void createDescriptorIfNeeded() {
        String name = ModelUtils.getModelName(this.getClass());
        if (this.uuid == null) {
            this.uuid = MongoDescriptorFactory.create(id, name);
        }
    }
}
