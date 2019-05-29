package com.extremum.jpa.factory;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class PostgresqlDescriptorFactoryAccessorConfigurator {
    private final PostgresqlDescriptorFactory postgresqlDescriptorFactory;

    @PostConstruct
    public void init() {
        StaticPostgresqlDescriptorFactoryAccessor.setFactory(postgresqlDescriptorFactory);
    }
}
