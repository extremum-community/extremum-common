package com.extremum.jpa.factory;

/**
 * @author rpuch
 */
public class StaticPostgresqlDescriptorFactoryAccessor {
    private static volatile PostgresqlDescriptorFacilities FACTORY_INSTANCE;

    public static PostgresqlDescriptorFacilities getFactory() {
        return FACTORY_INSTANCE;
    }

    public static void setFactory(PostgresqlDescriptorFacilities factory) {
        FACTORY_INSTANCE = factory;
    }
}
