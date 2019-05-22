package com.extremum.common.collection.dao.impl;

import com.extremum.common.collection.CollectionDescriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseCollectionDescriptorDaoImpl extends BaseCollectionDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 500000;
    private static final long DEFAULT_IDLE_TIME_DAYS = 30;

    public BaseCollectionDescriptorDaoImpl(RedissonClient redissonClient,
            CollectionDescriptorRepository repository, Codec codec,
            String descriptorsMapName, String coordinatesMapName) {
        this(redissonClient, repository, codec, descriptorsMapName, coordinatesMapName,
                DEFAULT_CACHE_SIZE, DEFAULT_IDLE_TIME_DAYS);
    }

    public BaseCollectionDescriptorDaoImpl(RedissonClient redissonClient, CollectionDescriptorRepository repository,
            Codec codec, String descriptorsMapName, String coordinatesMapName, int cacheSize, long idleTime) {
        super(repository,
                redissonClient.getLocalCachedMap(
                        descriptorsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, CollectionDescriptor>defaults()
                                .loader(descriptorIdMapLoader(repository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),
                redissonClient.getLocalCachedMap(
                        coordinatesMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(descriptorCoordinatesMapLoader(repository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE))
        );
    }

    private static MapLoader<String, CollectionDescriptor> descriptorIdMapLoader(
            CollectionDescriptorRepository repository) {
        return new MapLoader<String, CollectionDescriptor>() {
            @Override
            public CollectionDescriptor load(String key) {
                return repository.findByExternalId(key).orElse(null);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return repository.findAllExternalIds().stream()
                        .map(CollectionDescriptor::getExternalId)
                        .collect(Collectors.toList());
            }
        };
    }

    private static MapLoader<String, String> descriptorCoordinatesMapLoader(CollectionDescriptorRepository repository) {
        return new MapLoader<String, String>() {
            @Override
            public String load(String key) {
                return repository.findByCoordinatesString(key)
                        .map(CollectionDescriptor::getExternalId)
                        .orElse(null);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return repository.findAllCoordinatesStrings().stream()
                        .map(CollectionDescriptor::getCoordinatesString)
                        .collect(Collectors.toList());
            }
        };
    }
}
