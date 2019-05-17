package com.extremum.common.repository.jpa;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
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
 * This class emulates Spring Boot properties loading. It loads properties from the following sources (in this order):
 * 1. System properties (the ones passed via -D switch to java)
 * 2. Environment properties
 * 3. application.properties, application.xml on classpath
 * 4. application.yml, application.yaml on classpath
 *
 * If nothing is found on steps 3 and 4, an exception is thrown during construction.
 * If several files are found on steps 3 and 4, they are all added to the property sources, but the ones
 * that come earlier have a higher priority.
 *
 * @author rpuch
 */
public class ApplicationProperties {
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final PropertySource<?> propertySource;

    public ApplicationProperties() {
        List<PropertySource> propertySources = new ArrayList<>();

        propertySources.add(systemPropertySource());
        propertySources.add(environmentPropertySource());

        propertySources.addAll(loadWithLoader(new PropertiesPropertySourceLoader()));
        propertySources.addAll(loadWithLoader(new YamlPropertySourceLoader()));

        CompositePropertySource applicationProperties = new CompositePropertySource("application properties");
        propertySources.forEach(applicationProperties::addPropertySource);

        propertySource = applicationProperties;
    }

    private PropertiesPropertySource systemPropertySource() {
        return new PropertiesPropertySource("system properties", System.getProperties());
    }

    private SystemEnvironmentPropertySource environmentPropertySource() {
        return new SystemEnvironmentPropertySource("system environment", systemEnvMap());
    }

    private Map<String, Object> systemEnvMap() {
        return System.getenv().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<PropertySource> loadWithLoader(PropertySourceLoader loader) {
        List<PropertySource> propertySources = new ArrayList<>();

        for (String extension : loader.getFileExtensions()) {
            String fileName = "application." + extension;
            Resource propertiesResource = resourceLoader.getResource("classpath:/" + fileName);
            if (propertiesResource.exists()) {
                List<PropertySource<?>> propertiesPropertySources;
                try {
                    String resourceName = "applicationConfig: [classpath:/" + fileName + "]";
                    propertiesPropertySources = loader.load(resourceName, propertiesResource);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read " + fileName, e);
                }
                propertySources.addAll(propertiesPropertySources);
            }
        }

        return propertySources;
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
