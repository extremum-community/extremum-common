package com.extremum.jpa.factory;

/**
 * @author rpuch
 */
public class StaticPostgresqlDescriptorFactoryAccessor {
    private static volatile PostgresqlDescriptorFacilities FACILITIES_INSTANCE;

    public static PostgresqlDescriptorFacilities getFactory() {
        return FACILITIES_INSTANCE;
    }

    public static void setFactory(PostgresqlDescriptorFacilities factory) {
        FACILITIES_INSTANCE = factory;
    }
}
