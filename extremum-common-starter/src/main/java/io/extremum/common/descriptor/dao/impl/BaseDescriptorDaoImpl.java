package io.extremum.common.descriptor.dao.impl;

import io.extremum.common.redisson.FlexibleLocalCachedMapOptions;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.Descriptor.Readiness;
import org.redisson.RedissonLocalCachedMap.CacheValue;
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
                        FlexibleLocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .shouldBeCached(BaseDescriptorDaoImpl::descriptorWillNeverChange)
                                .loader(new DescriptorIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                ),

                redissonClient.getLocalCachedMap(
                        internalIdsMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorInternalIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                ),

                redissonClient.getLocalCachedMap(
                        collectionCoordinatesMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(new DescriptorCoordinatesMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
                )
        );
    }

    private static boolean descriptorWillNeverChange(CacheValue cacheValue) {
        return !isBlankDescriptor(cacheValue);
    }

    private static boolean isBlankDescriptor(CacheValue cacheValue) {
        if (cacheValue == null) {
            return false;
        }

        Object object = cacheValue.getValue();
        if (!(object instanceof Descriptor)) {
            return false;
        }

        Descriptor descriptor = (Descriptor) object;
        return descriptor.getReadiness() == Readiness.BLANK;
    }
}
