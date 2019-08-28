package io.extremum.starter;

import io.extremum.common.collection.dao.ReactiveCollectionDescriptorDao;
import io.extremum.common.collection.dao.impl.BaseReactiveCollectionDescriptorDaoImpl;
import io.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;

/**
 * @author rpuch
 */
public class CollectionDescriptorDaoFactory {
    public static ReactiveCollectionDescriptorDao createReactive(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonReactiveClient redissonClient, CollectionDescriptorRepository collectionDescriptorRepository) {
        Codec codec = RedisCodecFactory.codecFor(CollectionDescriptor.class);

        return new BaseReactiveCollectionDescriptorDaoImpl(redissonClient, collectionDescriptorRepository, codec,
                descriptorsProperties.getCollectionDescriptorsMapName(),
                redisProperties.getCacheSize(), redisProperties.getIdleTime());
    }

    private CollectionDescriptorDaoFactory() {}
}
