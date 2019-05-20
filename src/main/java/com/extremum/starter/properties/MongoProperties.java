package com.extremum.starter.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("mongo")
public class MongoProperties {
    public static final String REPOSITORY_PACKAGES_PROPERTY = "mongo.repository-packages";

    private String uri;
    private String dbName;
    private List<String> repositoryPackages;

}