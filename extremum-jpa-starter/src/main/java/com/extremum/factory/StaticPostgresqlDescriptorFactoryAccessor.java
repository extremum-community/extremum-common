package com.extremum.factory;

/**
 * @author rpuch
 */
public class StaticPostgresqlDescriptorFactoryAccessor {
    private static volatile PostgresqlDescriptorFactory FACTORY_INSTANCE;

    public static PostgresqlDescriptorFactory getFactory() {
        return FACTORY_INSTANCE;
    }

    public static void setFactory(PostgresqlDescriptorFactory factory) {
        FACTORY_INSTANCE = factory;
    }
}
