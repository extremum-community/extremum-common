package io.extremum.mongo.dbfactory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("mongo")
public class MongoDatabaseFactoryProperties {
    private String uri;
    // TODO: move this to MongoProperties
    private String serviceDbName;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(uri) || uri.contains("${")) {
            throw new IllegalStateException(String.format("mongo.uri has not been initialized, it is '%s'", uri));
        }
    }

}