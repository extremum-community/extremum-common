package com.extremum.common.models;

import com.extremum.sharedmodels.descriptor.Descriptor;
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
}
