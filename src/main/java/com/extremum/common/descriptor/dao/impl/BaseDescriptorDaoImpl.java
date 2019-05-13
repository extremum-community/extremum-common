package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseDescriptorDaoImpl extends BaseDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 500000;
    private static final long DEFAULT_IDLE_TIME = 30; //in days

    public BaseDescriptorDaoImpl(RedissonClient redissonClient, DescriptorRepository descriptorRepository,
                                 String descriptorsMapName, String internalIdsMapName, Codec codec) {
        this(redissonClient, descriptorRepository, descriptorsMapName, internalIdsMapName, codec,
                DEFAULT_CACHE_SIZE, DEFAULT_IDLE_TIME);
    }

    public BaseDescriptorDaoImpl(RedissonClient redissonClient, DescriptorRepository descriptorRepository,
                                 String descriptorsMapName, String internalIdsMapName, Codec codec,
            int cacheSize, long idleTime) {
        super(descriptorRepository,
                redissonClient.getLocalCachedMap(
                        descriptorsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .loader(descriptorIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),

                redissonClient.getLocalCachedMap(
                        internalIdsMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(descriptorInternalIdMapLoader(descriptorRepository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)));
    }

    private static MapLoader<String, Descriptor> descriptorIdMapLoader(DescriptorRepository descriptorRepository) {
        return new MapLoader<String, Descriptor>() {
            @Override
            public Descriptor load(String key) {
                return descriptorRepository.findById(key).orElse(null);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return descriptorRepository.findAllExternalIds().stream()
                        .map(Descriptor::getExternalId)
                        .collect(Collectors.toList());
            }
        };
    }

    private static MapLoader<String, String> descriptorInternalIdMapLoader(DescriptorRepository descriptorRepository) {
        return new MapLoader<String, String>() {
            @Override
            public String load(String key) {
                return descriptorRepository.findByInternalId(key)
                        .map(Descriptor::getExternalId)
                        .orElse(null);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return descriptorRepository.findAllInternalIds().stream()
                        .map(Descriptor::getInternalId)
                        .collect(Collectors.toList());
            }
        };
    }
}
