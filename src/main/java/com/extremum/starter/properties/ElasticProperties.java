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
@ConfigurationProperties("elastic")
public class ElasticProperties {
    private List<ElasticProperties.Host> hosts;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Host {
        public String host;
        public int port;
        public String protocol;
    }
}