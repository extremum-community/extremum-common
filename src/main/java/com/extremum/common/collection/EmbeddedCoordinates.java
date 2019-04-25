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
public class EmbeddedCoordinates implements Serializable {
    private Descriptor hostId;
    private String hostFieldName;

    private EmbeddedCoordinates() {
    }

    public String toCoordinatesString() {
        return "EMBEDDED/" + hostId.getExternalId() + "/" + hostFieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmbeddedCoordinates that = (EmbeddedCoordinates) o;
        return Objects.equals(hostId, that.hostId) &&
                Objects.equals(hostFieldName, that.hostFieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostId, hostFieldName);
    }
}
