package com.extremum.common.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
public class CollectionCoordinates implements Serializable {
    private EmbeddedCoordinates embeddedCoordinates;

    private CollectionCoordinates() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CollectionCoordinates that = (CollectionCoordinates) o;
        return Objects.equals(embeddedCoordinates, that.embeddedCoordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddedCoordinates);
    }
}
