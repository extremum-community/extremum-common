package io.extremum.sharedmodels.descriptor;

import lombok.*;

import java.io.Serializable;

/**
 * @author rpuch
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CollectionCoordinates implements Serializable {
    private OwnedCoordinates ownedCoordinates;
    private FreeCoordinates freeCoordinates;

    public CollectionCoordinates(OwnedCoordinates ownedCoordinates) {
        this.ownedCoordinates = ownedCoordinates;
    }

    public CollectionCoordinates(FreeCoordinates freeCoordinates) {
        this.freeCoordinates = freeCoordinates;
    }
}
