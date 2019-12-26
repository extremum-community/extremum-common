package io.extremum.mongo.model;

import io.extremum.common.model.VersionedModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.ZonedDateTime;

@Getter
@Setter
@CompoundIndexes({
        @CompoundIndex(def = "{'lineageId': 1, 'start': 1, 'end': 1}", name = "lineageId_start_end"),
        @CompoundIndex(def = "{'lineageId': 1, 'currentSnapshot': 1}", name = "lineageId_currentSnapshot"),
        @CompoundIndex(def = "{'lineageId': 1, 'version': 1}", unique = true, name = "lineageId_version")
})
public abstract class MongoVersionedModel implements VersionedModel<ObjectId> {
    @Transient
    private Descriptor uuid;

    @Id
    private ObjectId snapshotId;

    @Indexed
    private ObjectId lineageId;

    @Indexed
    private ZonedDateTime created;
    private ZonedDateTime start;
    private ZonedDateTime end;
    @Indexed
    private boolean currentSnapshot;

    private Long version;

    @Indexed
    private Boolean deleted = false;

    @Override
    public ObjectId getId() {
        return getLineageId();
    }

    @Override
    public void setId(ObjectId id) {
        setLineageId(id);
    }
}
