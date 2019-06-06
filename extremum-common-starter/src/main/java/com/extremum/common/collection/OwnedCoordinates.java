package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.*;

import java.io.Serializable;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class OwnedCoordinates implements Serializable {
    private Descriptor hostId;
    private String hostAttributeName;

    public String toCoordinatesString() {
        return "OWNED/" + hostId.getExternalId() + "/" + hostAttributeName;
    }
}
