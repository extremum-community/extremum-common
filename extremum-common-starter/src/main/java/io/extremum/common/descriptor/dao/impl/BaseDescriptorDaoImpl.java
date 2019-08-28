package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

public class BaseDescriptorDaoImpl extends BaseDescriptorDao {
    public BaseDescriptorDaoImpl(RedissonClient redissonClient, DescriptorRepository descriptorRepository,
                                 String descriptorsMapName, String internalIdsMapName,
                                 String collectionCoordinatesMapName,
                                 Codec codec,
                                 int cacheSize, long idleTime) {
        super(descriptorRepository,
                redissonClient.getLocalCachedMap(
                        descriptorsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .loader(new DescriptorIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                redissonClient.getLocalCachedMap(
                        internalIdsMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorInternalIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                redissonClient.getLocalCachedMap(
                        collectionCoordinatesMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorCoordinatesMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE))
        );
    }
}
