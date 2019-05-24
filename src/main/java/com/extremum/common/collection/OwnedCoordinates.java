package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class OwnedCoordinates implements Serializable {
    private Descriptor hostId;
    private String hostFieldName;

    public String toCoordinatesString() {
        return "OWNED/" + hostId.getExternalId() + "/" + hostFieldName;
    }
}
