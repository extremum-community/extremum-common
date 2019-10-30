package io.extremum.common.redisson;

import org.redisson.Redisson;
import org.redisson.RedissonLocalCachedMap;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.cache.Cache;
import org.redisson.cache.CacheKey;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

public class ExtremumRedisson extends Redisson {

    public ExtremumRedisson(Config redissonConfig) {
        super(redissonConfig);

        if (redissonConfig.isReferenceEnabled()) {
            enableRedissonReferenceSupport();
        }
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec,
                                                          LocalCachedMapOptions<K, V> options) {
        return new RedissonLocalCachedMap<K, V>(codec, connectionManager.getCommandExecutor(), name, options,
                evictionScheduler, this) {
            @Override
            protected Cache<CacheKey, CacheValue> createCache(LocalCachedMapOptions<K, V> options) {
                Cache<CacheKey, CacheValue> delegate = super.createCache(options);

                if (options instanceof FlexibleLocalCachedMapOptions) {
                    return new FlexibleCache(delegate,
                            ((FlexibleLocalCachedMapOptions<K, V>) options).getShouldBeCached());
                } else {
                    return new FlexibleCache(delegate);
                }
            }
        };
    }
}
