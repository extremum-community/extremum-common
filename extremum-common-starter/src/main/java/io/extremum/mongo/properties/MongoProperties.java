package io.extremum.mongo.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("mongo")
public class MongoProperties {
    public static final String REPOSITORY_PACKAGES_PROPERTY = "mongo.repository-packages";

    private String uri;
    private String serviceDbName;
    private String descriptorsDbName;
    private List<String> modelPackages = new ArrayList<>();
    private List<String> repositoryPackages;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(uri) || uri.contains("${")) {
            throw new IllegalStateException(String.format("mongo.uri has not been initialized, it is '%s'", uri));
        }
    }

}