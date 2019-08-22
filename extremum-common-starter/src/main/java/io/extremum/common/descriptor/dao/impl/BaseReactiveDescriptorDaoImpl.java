package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

public class BaseReactiveDescriptorDaoImpl extends BaseReactiveDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 500000;
    private static final long DEFAULT_IDLE_TIME_DAYS = 30;

    public BaseReactiveDescriptorDaoImpl(RedissonReactiveClient redissonClient,
                                         DescriptorRepository descriptorRepository,
                                         String descriptorsMapName, String internalIdsMapName, Codec codec) {
        this(redissonClient, descriptorRepository, descriptorsMapName, internalIdsMapName, codec,
                DEFAULT_CACHE_SIZE, DEFAULT_IDLE_TIME_DAYS);
    }

    public BaseReactiveDescriptorDaoImpl(RedissonReactiveClient redissonClient,
                                         DescriptorRepository descriptorRepository,
                                         String descriptorsMapName, String internalIdsMapName, Codec codec,
                                         int cacheSize, long idleTime) {
        super(
                // TODO: here, we use getMap() instead of getMapCache() because the latter causes weird runtime exceptions
                redissonClient.getMap(
                        descriptorsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .loader(new DescriptorIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                ),

                // TODO: here, we use getMap() instead of getMapCache() because the latter causes weird runtime exceptions
                redissonClient.getMap(
                        internalIdsMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorInternalIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                )
        );
    }
}
