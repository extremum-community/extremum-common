package com.extremum.common.descriptor.factory.impl;

/**
 * @author rpuch
 */
public class StaticPostgresqlDescriptorFactoryAccessor {
    private static PostgresqlDescriptorFactory FACTORY_INSTANCE;

    public static PostgresqlDescriptorFactory getFactory() {
        return FACTORY_INSTANCE;
    }

    public static void setFactory(PostgresqlDescriptorFactory factory) {
        FACTORY_INSTANCE = factory;
    }
}
