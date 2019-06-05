package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Document("collection-descriptors")
public final class CollectionDescriptor implements Serializable {
    @Id
    private String externalId;
    private Type type;
    private CollectionCoordinates coordinates;
    @Indexed
    private String coordinatesString;

    @CreatedDate
    private ZonedDateTime created;
    @LastModifiedDate
    private ZonedDateTime modified;
    @Setter
    private boolean deleted;

    private CollectionDescriptor() {
    }

    public CollectionDescriptor(String externalId) {
        this.externalId = externalId;
    }

    private CollectionDescriptor(Type type, CollectionCoordinates coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public static CollectionDescriptor forOwned(Descriptor hostId, String hostAttributeName) {
        return new CollectionDescriptor(Type.OWNED,
                new CollectionCoordinates(new OwnedCoordinates(hostId, hostAttributeName)));
    }

    @Override
    public String toString() {
        return externalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CollectionDescriptor that = (CollectionDescriptor) o;
        if (externalId != null && that.externalId != null) {
            return Objects.equals(externalId, that.externalId);
        }
        return type == that.type && Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        if (externalId != null) {
            return externalId.hashCode();
        }
        return Objects.hash(type, coordinates);
    }

    public String toCoordinatesString() {
        return type.toCoordinatesString(coordinates);
    }

    public void generateExternalIdIfNeeded() {
        if (externalId == null) {
            externalId = UUID.randomUUID().toString();
        }
    }

    public void refreshCoordinatesString() {
        coordinatesString = toCoordinatesString();
    }

    public enum Type {
        /**
         * Collection is identified by host (owning) entity and host field name.
         */
        OWNED {
            @Override
            String toCoordinatesString(CollectionCoordinates coordinates) {
                return coordinates.getOwnedCoordinates().toCoordinatesString();
            }
        };

        abstract String toCoordinatesString(CollectionCoordinates coordinates);
    }

    public enum FIELDS {
        externalId, type, coordinates, coordinatesString
    }
}
