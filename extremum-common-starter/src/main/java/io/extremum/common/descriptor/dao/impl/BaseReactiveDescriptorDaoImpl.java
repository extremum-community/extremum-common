package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

import java.util.concurrent.TimeUnit;

public class BaseReactiveDescriptorDaoImpl extends BaseReactiveDescriptorDao {
    public BaseReactiveDescriptorDaoImpl(RedissonReactiveClient redissonClient,
                                         DescriptorRepository descriptorRepository,
                                         ReactiveMongoOperations reactiveMongoOperations,
                                         String descriptorsMapName, String internalIdsMapName,
                                         String collectionCoordinatesMapName,
                                         Codec codec,
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
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
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
                ),

                // TODO: here, we use getMap() instead of getMapCache() because the latter causes weird runtime exceptions
                redissonClient.getMap(
                        collectionCoordinatesMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorCoordinatesMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                reactiveMongoOperations);
    }
}
