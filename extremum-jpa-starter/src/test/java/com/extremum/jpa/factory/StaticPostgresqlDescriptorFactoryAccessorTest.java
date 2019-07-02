package com.extremum.jpa.factory;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StaticPostgresqlDescriptorFactoryAccessorTest {
    private static final DescriptorFactory NOT_USED = null;

    @Test
    void test() {
        PostgresqlDescriptorFactory factory = new PostgresqlDescriptorFactory(NOT_USED);

        StaticPostgresqlDescriptorFactoryAccessor.setFactory(factory);

        assertThat(StaticPostgresqlDescriptorFactoryAccessor.getFactory(), is(factory));
    }
}