package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.descriptor.dao.impl.CarefulMapLoader;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

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
                                .loader(new CollectionDescriptorIdMapLoader(repository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)),
                redissonClient.getLocalCachedMap(
                        coordinatesMapName,
                        LocalCachedMapOptions
                                .<String, String>defaults()
                                .loader(descriptorCoordinatesMapLoader(repository))
                                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                                .cacheSize(cacheSize)
                                .maxIdle(idleTime, TimeUnit.DAYS)
                                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE))
        );
    }

    private static MapLoader<String, String> descriptorCoordinatesMapLoader(CollectionDescriptorRepository repository) {
        return new CarefulMapLoader<String, String>() {
            @Override
            public String load(String key) {
                return repository.findByCoordinatesString(key)
                        .map(CollectionDescriptor::getExternalId)
                        .orElse(null);
            }
        };
    }

}
