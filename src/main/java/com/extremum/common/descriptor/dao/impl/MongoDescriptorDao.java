package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class MongoDescriptorDao extends BaseDescriptorDao {
    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_TTL = 0;    // in seconds

    public MongoDescriptorDao(RedissonClient redissonClient, Datastore mongoDatastore,
                              String descriptorsMapName, String internalIdsMapName) {
        this(redissonClient, mongoDatastore, descriptorsMapName, internalIdsMapName, DEFAULT_CACHE_SIZE, DEFAULT_TTL);
    }

    public MongoDescriptorDao(RedissonClient redissonClient, Datastore mongoDatastore,
                              String descriptorsMapName, String internalIdsMapName, int cacheSize, long timeToLive) {
        super(redissonClient.getLocalCachedMap(
                    descriptorsMapName,
                    LocalCachedMapOptions
                            .<String, Descriptor> defaults()
                            .writer(descriptorMapWriter(mongoDatastore))
                            .loader(descriptorIdMapLoader(mongoDatastore))
                            .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                            .cacheSize(cacheSize)
                            .maxIdle(timeToLive, TimeUnit.SECONDS)
                            .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),
            redissonClient.getLocalCachedMap(
                        internalIdsMapName,
                        LocalCachedMapOptions
                                .<String, String> defaults()
                                .loader(descriptorInternalIdMapLoader(mongoDatastore))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(timeToLive, TimeUnit.SECONDS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)));
    }

    private static MapWriter<String, Descriptor> descriptorMapWriter(Datastore descriptorsStore) {
        return new MapWriter<String, Descriptor>() {
            @Override
            public void write(String key, Descriptor value) {
                descriptorsStore.save(value);
            }

            @Override
            public void writeAll(Map<String, Descriptor> map) {
                descriptorsStore.save(map.values());
            }

            @Override
            public void delete(String key) {
                descriptorsStore.delete(Descriptor.class, key);
            }

            @Override
            public void deleteAll(Collection<String> keys) {
                descriptorsStore.delete(Descriptor.class, keys);
            }
        };
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
