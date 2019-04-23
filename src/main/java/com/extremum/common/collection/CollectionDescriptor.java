package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.Getter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.io.Serializable;

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

    public String toCoordinatesString() {
        return type.toCoordinatesString(coordinates);
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
        externalId, type, coordinates
    }
}
