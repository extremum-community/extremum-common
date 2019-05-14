package com.extremum.common.descriptor.factory.impl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
public class StaticPostgresqlDescriptorFactoryAccessorTest {
    @Test
    public void test() {
        PostgresqlDescriptorFactory factory = mock(PostgresqlDescriptorFactory.class);

        StaticPostgresqlDescriptorFactoryAccessor.setFactory(factory);

        assertThat(StaticPostgresqlDescriptorFactoryAccessor.getFactory(), is(factory));
    }
}