package io.extremum.sharedmodels.descriptor;

import lombok.*;

import java.io.Serializable;

/**
 * @author rpuch
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CollectionCoordinates implements Serializable {
    private OwnedCoordinates ownedCoordinates;
}
