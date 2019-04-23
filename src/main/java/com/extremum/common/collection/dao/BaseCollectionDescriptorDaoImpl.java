package com.extremum.common.collection.dao;

import com.extremum.common.collection.CollectionDescriptor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseCollectionDescriptorDaoImpl extends BaseCollectionDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 500000;
    private static final long DEFAULT_IDLE_TIME_DAYS = 30;

    public BaseCollectionDescriptorDaoImpl(RedissonClient redissonClient, Datastore mongoDatastore,
                                 String descriptorsMapName) {
        this(redissonClient, mongoDatastore, descriptorsMapName, DEFAULT_CACHE_SIZE, DEFAULT_IDLE_TIME_DAYS);
    }

    public BaseCollectionDescriptorDaoImpl(RedissonClient redissonClient, Datastore mongoDatastore,
                                 String descriptorsMapName, int cacheSize, long idleTime) {
        super(mongoDatastore,
                redissonClient.getLocalCachedMap(
                        descriptorsMapName,
                        LocalCachedMapOptions
                                .<String, CollectionDescriptor>defaults()
                                .loader(descriptorIdMapLoader(mongoDatastore))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)));
    }

    private static MapLoader<String, CollectionDescriptor> descriptorIdMapLoader(Datastore descriptorsStore) {
        return new MapLoader<String, CollectionDescriptor>() {
            @Override
            public CollectionDescriptor load(String key) {
                return descriptorsStore.get(CollectionDescriptor.class, key);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return descriptorsStore.find(CollectionDescriptor.class).asKeyList().stream()
                        .map(Key::getId)
                        .map(String.class::cast)
                        .collect(Collectors.toList());
            }
        };
    }
}
