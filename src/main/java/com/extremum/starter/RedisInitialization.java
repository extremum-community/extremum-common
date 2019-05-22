package com.extremum.starter;

import com.extremum.starter.properties.RedisProperties;

/**
 * @author rpuch
 */
class RedisInitialization {
    static boolean noRedis(RedisProperties redisProperties) {
        return redisProperties.getCacheSize() == 0 && redisProperties.getIdleTime() == 0;
    }

    private RedisInitialization() {}
}
