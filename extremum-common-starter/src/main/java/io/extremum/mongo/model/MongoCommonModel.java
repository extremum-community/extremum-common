package io.extremum.mongo.model;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
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

    public void setDeleted(Boolean newDeleted) {
        if (newDeleted == null) {
            throw new IllegalArgumentException("deleted cannot be null");
        }

        this.deleted = newDeleted;
    }
}
