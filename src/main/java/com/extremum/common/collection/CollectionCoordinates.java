package com.extremum.common.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
public class CollectionCoordinates implements Serializable {
    private final EmbeddedCoordinates embeddedCoordinates;
}
