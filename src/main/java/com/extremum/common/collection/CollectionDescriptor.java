package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.Getter;

/**
 * @author rpuch
 */
@Getter
public final class CollectionDescriptor {
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

    public enum Type {
        /**
         * Collection is identified by host (embedding) entity and host field name.
         */
        EMBEDDED
    }

    public enum FIELDS {
        externalId, type, coordinates
    }
}
