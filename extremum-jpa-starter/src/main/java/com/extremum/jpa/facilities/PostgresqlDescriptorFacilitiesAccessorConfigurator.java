package com.extremum.jpa.facilities;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class PostgresqlDescriptorFacilitiesAccessorConfigurator {
    private final PostgresqlDescriptorFacilities postgresqlDescriptorFacilities;

    @PostConstruct
    public void init() {
        StaticPostgresqlDescriptorFactoryAccessor.setFacilities(postgresqlDescriptorFacilities);
    }
}
