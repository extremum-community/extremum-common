package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
public class OwnedCoordinates implements Serializable {
    private Descriptor hostId;
    private String hostFieldName;

    private OwnedCoordinates() {
    }

    public String toCoordinatesString() {
        return "OWNED/" + hostId.getExternalId() + "/" + hostFieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OwnedCoordinates that = (OwnedCoordinates) o;
        return Objects.equals(hostId, that.hostId) &&
                Objects.equals(hostFieldName, that.hostFieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostId, hostFieldName);
    }
}
