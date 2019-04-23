package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
public class EmbeddedCoordinates {
    private final Descriptor hostId;
    private final String hostFieldName;
}
