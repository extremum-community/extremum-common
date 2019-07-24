package com.extremum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.watch.subscription")
@Getter
@Setter
public class SubscriptionProperties {
    private int timeToLive = 30; // in days
    private int idleTime = 7; // in days
}
