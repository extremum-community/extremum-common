package io.extremum.starter;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.common.collection.dao.impl.BaseCollectionDescriptorDaoImpl;
import io.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

/**
 * @author rpuch
 */
public class CollectionDescriptorDaoFactory {
    public static CollectionDescriptorDao create(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonClient redissonClient, CollectionDescriptorRepository collectionDescriptorRepository) {
        Codec codec = RedisCodecFactory.codecFor(CollectionDescriptor.class);

        if (noRedis(redisProperties)) {
            return new BaseCollectionDescriptorDaoImpl(redissonClient, collectionDescriptorRepository, codec,
                    descriptorsProperties.getCollectionDescriptorsMapName(),
                    descriptorsProperties.getCollectionCoordinatesMapName());
        } else {
            return new BaseCollectionDescriptorDaoImpl(redissonClient, collectionDescriptorRepository, codec,
                    descriptorsProperties.getCollectionDescriptorsMapName(),
                    descriptorsProperties.getCollectionCoordinatesMapName(),
                    redisProperties.getCacheSize(), redisProperties.getIdleTime());
        }
    }

    private static boolean noRedis(RedisProperties redisProperties) {
        return RedisInitialization.noRedis(redisProperties);
    }

    private CollectionDescriptorDaoFactory() {}
}
