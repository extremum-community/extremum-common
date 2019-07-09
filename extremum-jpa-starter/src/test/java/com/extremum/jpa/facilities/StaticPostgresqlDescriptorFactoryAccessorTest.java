package com.extremum.jpa.facilities;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StaticPostgresqlDescriptorFactoryAccessorTest {
    private static final DescriptorSaver NOT_USED = null;

    @Test
    void test() {
        PostgresqlDescriptorFacilities factory = new PostgresqlDescriptorFacilitiesImpl(new DescriptorFactory(), NOT_USED);

        StaticPostgresqlDescriptorFactoryAccessor.setFacilities(factory);

        assertThat(StaticPostgresqlDescriptorFactoryAccessor.getFacilities(), is(factory));
    }
}