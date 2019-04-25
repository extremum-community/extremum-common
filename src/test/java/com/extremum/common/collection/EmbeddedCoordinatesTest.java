package com.extremum.common.collection;

import com.extremum.common.descriptor.Descriptor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author rpuch
 */
public class EmbeddedCoordinatesTest {
    @Test
    public void testToCoordinatesString() {
        EmbeddedCoordinates coordinates = new EmbeddedCoordinates(new Descriptor("external-id"), "items");
        assertThat(coordinates.toCoordinatesString(), is("EMBEDDED/external-id/items"));
    }
}