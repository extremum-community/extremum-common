package io.extremum.sharedmodels.descriptor;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public final class CollectionDescriptor implements Serializable {
    private Type type;
    private CollectionCoordinates coordinates;
    private String coordinatesString;

    @Setter
    private boolean deleted;

    private CollectionDescriptor() {
    }

    public CollectionDescriptor(String externalId) {
        // TODO: remove
        throw new UnsupportedOperationException("Please remove this constructor");
    }

    private CollectionDescriptor(Type type, CollectionCoordinates coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public static CollectionDescriptor forOwned(Descriptor hostId, String hostAttributeName) {
        return new CollectionDescriptor(Type.OWNED,
                new CollectionCoordinates(new OwnedCoordinates(hostId, hostAttributeName)));
    }

    public String getExternalId() {
        // TODO: remove this
        throw new UnsupportedOperationException("Please remove this method");
    }

    @Override
    public String toString() {
        return coordinatesString;
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
        return type == that.type && Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinates);
    }

    public String toCoordinatesString() {
        return type.toCoordinatesString(coordinates);
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
        type, coordinates, coordinatesString
    }
}
