package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.CollectionDescriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

public class BaseReactiveCollectionDescriptorDaoImpl extends BaseReactiveCollectionDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 500000;
    private static final long DEFAULT_IDLE_TIME_DAYS = 30;

    public BaseReactiveCollectionDescriptorDaoImpl(RedissonReactiveClient redissonClient,
                                                   CollectionDescriptorRepository repository, Codec codec,
                                                   String descriptorsMapName) {
        this(redissonClient, repository, codec, descriptorsMapName,
                DEFAULT_CACHE_SIZE, DEFAULT_IDLE_TIME_DAYS);
    }

    public BaseReactiveCollectionDescriptorDaoImpl(RedissonReactiveClient redissonClient,
                                                   CollectionDescriptorRepository repository,
                                                   Codec codec, String descriptorsMapName,
                                                   int cacheSize, long idleTime) {
        super(
                // TODO: here, we use getMap() instead of getMapCache() because the latter causes weird runtime exceptions
                redissonClient.getMap(
                        descriptorsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, CollectionDescriptor>defaults()
                                .loader(new CollectionDescriptorIdMapLoader(repository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE))
        );
    }

}
