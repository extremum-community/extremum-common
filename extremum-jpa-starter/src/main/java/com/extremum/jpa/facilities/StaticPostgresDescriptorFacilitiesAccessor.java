package com.extremum.jpa.facilities;

/**
 * @author rpuch
 */
public class StaticPostgresDescriptorFacilitiesAccessor {
    private static volatile PostgresDescriptorFacilities FACILITIES_INSTANCE;

    public static PostgresDescriptorFacilities getFacilities() {
        return FACILITIES_INSTANCE;
    }

    public static void setFacilities(PostgresDescriptorFacilities facilities) {
        FACILITIES_INSTANCE = facilities;
    }
}
