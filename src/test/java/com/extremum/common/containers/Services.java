package com.extremum.common.containers;

import org.testcontainers.containers.GenericContainer;

/**
 * @author rpuch
 */
public class Services {
    static {
        GenericContainer postgres = new GenericContainer("postgres:11.3-alpine").withExposedPorts(5432);
        postgres.start();
        String postgresUrl = String.format("jdbc:postgresql://%s:%d/%s",
                postgres.getContainerIpAddress(), postgres.getFirstMappedPort(), "postgres");
        System.setProperty("jpa.uri", postgresUrl);
    }
}
