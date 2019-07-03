package com.extremum.jpa.factory;

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
        PostgresqlDescriptorFacilities factory = new PostgresqlDescriptorFacilities(NOT_USED);

        StaticPostgresqlDescriptorFactoryAccessor.setFactory(factory);

        assertThat(StaticPostgresqlDescriptorFactoryAccessor.getFactory(), is(factory));
    }
}