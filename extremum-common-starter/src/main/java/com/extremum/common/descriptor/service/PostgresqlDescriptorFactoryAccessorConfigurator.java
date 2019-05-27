package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.factory.impl.PostgresqlDescriptorFactory;
import com.extremum.common.descriptor.factory.impl.StaticPostgresqlDescriptorFactoryAccessor;
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
