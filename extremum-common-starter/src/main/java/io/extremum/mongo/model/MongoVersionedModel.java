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
import java.util.Objects;

@Getter
@Setter
@CompoundIndexes({
        @CompoundIndex(def = "{'historyId': 1, 'start': 1, 'end': 1}", name = "historyId_start_end"),
        @CompoundIndex(def = "{'historyId': 1, 'version': 1}", unique = true, name = "historyId_version")
})
public abstract class MongoVersionedModel implements VersionedModel<ObjectId> {
    @Transient
    private Descriptor uuid;

    @Id
    private ObjectId snapshotId;

    @Indexed
    private ObjectId historyId;

    @Indexed
    private ZonedDateTime created;
    private ZonedDateTime start;
    private ZonedDateTime end;

    private long version;

    @Indexed
    private Boolean deleted = false;

    @Override
    public ObjectId getId() {
        return getHistoryId();
    }

    @Override
    public void setId(ObjectId id) {
        setHistoryId(id);
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        Objects.requireNonNull(version, "version cannot be null");
        this.version = version;
    }
}
