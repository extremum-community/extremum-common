package com.extremum.elasticsearch.properties;

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
@ConfigurationProperties("elastic")
public class ElasticProperties {
    private List<ElasticProperties.Host> hosts;
    private String username;
    private String password;

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