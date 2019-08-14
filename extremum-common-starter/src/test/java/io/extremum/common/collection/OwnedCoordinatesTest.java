package io.extremum.common.collection;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
public class OwnedCoordinatesTest {
    @Test
    public void testToCoordinatesString() {
        OwnedCoordinates coordinates = new OwnedCoordinates(new Descriptor("external-id"), "items");
        assertThat(coordinates.toCoordinatesString(), is("OWNED/external-id/items"));
    }
}