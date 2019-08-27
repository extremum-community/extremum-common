package io.extremum.common.collection.dao.impl;

import io.extremum.common.descriptor.dao.impl.CarefulMapLoader;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.Codec;

import java.util.concurrent.TimeUnit;

public class BaseCollectionDescriptorDaoImpl extends BaseCollectionDescriptorDao {
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
