package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.Getter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PrePersist;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author rpuch
 */
@Getter
@Entity(value = "collection-descriptors", noClassnameStored = true)
public final class CollectionDescriptor implements Serializable {
    @Id
    private String externalId;
    private Type type;
    private CollectionCoordinates coordinates;
    @Indexed
    private String coordinatesString;

    private CollectionDescriptor() {
    }

    public CollectionDescriptor(String externalId) {
        this.externalId = externalId;
    }

    public CollectionDescriptor(Descriptor hostId, String hostFieldName) {
        this.type = Type.EMBEDDED;
        this.coordinates = new CollectionCoordinates(new EmbeddedCoordinates(hostId, hostFieldName));
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

    @PrePersist
    public void generateExternalIdIfNeeded() {
        if (externalId == null) {
            externalId = UUID.randomUUID().toString();
        }
    }

    @PrePersist
    public void refreshCoordinatesString() {
        coordinatesString = toCoordinatesString();
    }

    public enum Type {
        /**
         * Collection is identified by host (embedding) entity and host field name.
         */
        EMBEDDED {
            @Override
            String toCoordinatesString(CollectionCoordinates coordinates) {
                return coordinates.getEmbeddedCoordinates().toCoordinatesString();
            }
        };

        abstract String toCoordinatesString(CollectionCoordinates coordinates);
    }

    public enum FIELDS {
        externalId, type, coordinates, coordinatesString
    }
}
