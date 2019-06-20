package com.extremum.elasticsearch.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("elasticsearch")
@Valid
public class ElasticsearchProperties {
    private List<ElasticsearchProperties.Host> hosts;
    private String username;
    private String password;
    @NotEmpty
    private List<String> repositoryPackages;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Host {
        private String host;
        private int port;
        private String protocol;
    }
}