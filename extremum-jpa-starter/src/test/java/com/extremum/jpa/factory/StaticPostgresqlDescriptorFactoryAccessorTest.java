package com.extremum.jpa.factory;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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