package com.extremum.jpa.facilities;

/**
 * @author rpuch
 */
public class StaticPostgresqlDescriptorFactoryAccessor {
    private static volatile PostgresqlDescriptorFacilities FACILITIES_INSTANCE;

    public static PostgresqlDescriptorFacilities getFacilities() {
        return FACILITIES_INSTANCE;
    }

    public static void setFacilities(PostgresqlDescriptorFacilities facilities) {
        FACILITIES_INSTANCE = facilities;
    }
}
