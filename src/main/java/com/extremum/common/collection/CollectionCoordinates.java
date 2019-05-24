package com.extremum.common.collection;

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
public class CollectionCoordinates implements Serializable {
    private OwnedCoordinates ownedCoordinates;
}
