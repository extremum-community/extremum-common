package com.extremum.jpa.facilities;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StaticPostgresDescriptorFacilitiesAccessorTest {
    private static final DescriptorSaver NOT_USED = null;

    @Test
    void test() {
        PostgresDescriptorFacilities factory = new PostgresDescriptorFacilitiesImpl(new DescriptorFactory(), NOT_USED);

        StaticPostgresDescriptorFacilitiesAccessor.setFacilities(factory);

        assertThat(StaticPostgresDescriptorFacilitiesAccessor.getFacilities(), is(factory));
    }
}