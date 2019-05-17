package com.extremum.common.repository.jpa;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
public class ApplicationProperties {
    private final PropertySource<?> propertySource;

    public ApplicationProperties() {
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        List<PropertySource> propertySources = new ArrayList<>();
        propertySources.add(new PropertiesPropertySource("system properties", System.getProperties()));
        propertySources.add(new SystemEnvironmentPropertySource("system environment", systemEnvMap()));

        Resource yamlResource = resourceLoader.getResource("classpath:/application.yml");
        if (yamlResource.exists()) {
            List<PropertySource<?>> yamlPropertySources;
            try {
                yamlPropertySources = new YamlPropertySourceLoader().load(
                        "applicationConfig: [classpath:/application.yml]", yamlResource);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read application.yml", e);
            }
            propertySources.addAll(yamlPropertySources);
        }

        Resource propertiesResource = resourceLoader.getResource("classpath:/application.properties");
        if (propertiesResource.exists()) {
            List<PropertySource<?>> propertiesPropertySources;
            try {
                propertiesPropertySources = new PropertiesPropertySourceLoader().load(
                        "applicationConfig: [classpath:/application.properties]", propertiesResource);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read application.yml", e);
            }
            propertySources.addAll(propertiesPropertySources);
        }

        CompositePropertySource applicationProperties = new CompositePropertySource("application properties");
        propertySources.forEach(applicationProperties::addPropertySource);

        propertySource = applicationProperties;
    }

    private Map<String, Object> systemEnvMap() {
        return System.getenv().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<String> getTrimmedStringList(String name) {
        Object value = propertySource.getProperty(name);
        if (value == null) {
            return Collections.emptyList();
        }

        if (!(value instanceof String)) {
            throw new IllegalStateException("Expected to get a String but got " + value);
        }
        String stringValue = (String) value;

        return Arrays.stream(stringValue.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
