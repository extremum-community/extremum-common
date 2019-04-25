package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseDescriptorDaoImpl extends BaseDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 500000;
    private static long DEFAULT_IDLE_TIME = 30; //in days

    public BaseDescriptorDaoImpl(RedissonClient redissonClient, Datastore mongoDatastore,
                                 String descriptorsMapName, String internalIdsMapName, Codec codec) {
        this(redissonClient, mongoDatastore, descriptorsMapName, internalIdsMapName, codec, DEFAULT_CACHE_SIZE, DEFAULT_IDLE_TIME);
    }

    public BaseDescriptorDaoImpl(RedissonClient redissonClient, Datastore mongoDatastore,
                                 String descriptorsMapName, String internalIdsMapName, Codec codec, int cacheSize, long idleTime) {
        super(mongoDatastore,
                redissonClient.getLocalCachedMap(
                        descriptorsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, Descriptor>defaults()
                                .loader(descriptorIdMapLoader(mongoDatastore))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),
                redissonClient.getLocalCachedMap(
                        internalIdsMapName,
                        codec,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(descriptorInternalIdMapLoader(mongoDatastore))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)));
    }

    private static MapLoader<String, Descriptor> descriptorIdMapLoader(Datastore descriptorsStore) {
        return new MapLoader<String, Descriptor>() {
            @Override
            public Descriptor load(String key) {
                return descriptorsStore.get(Descriptor.class, key);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return descriptorsStore.find(Descriptor.class).asKeyList().stream()
                        .map(Key::getId)
                        .map(String.class::cast)
                        .collect(Collectors.toList());
            }
        };
    }

    private static MapLoader<String, String> descriptorInternalIdMapLoader(Datastore descriptorsStore) {
        return new MapLoader<String, String>() {
            @Override
            public String load(String key) {
                Descriptor descriptor = descriptorsStore.find(Descriptor.class).field("internalId").equal(key).get();
                return descriptor != null
                        ? descriptor.getExternalId()
                        : null;
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return descriptorsStore.find(Descriptor.class).asList().stream()
                        .map(Descriptor::getInternalId)
                        .collect(Collectors.toList());
            }
        };
    }
}
