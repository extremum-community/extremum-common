package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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