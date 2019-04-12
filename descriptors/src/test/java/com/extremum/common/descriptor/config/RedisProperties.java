package com.extremum.common.descriptor.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("redis")
public class RedisProperties {

    private String uri;

    private int cacheSize;

    private long idleTime;
}
