package io.extremum.starter;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import io.extremum.common.descriptor.dao.impl.BaseReactiveDescriptorDaoImpl;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

public class DescriptorDaoFactory {
    public static DescriptorDao create(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonClient redissonClient, DescriptorRepository descriptorRepository) {
        Codec codec = RedisCodecFactory.codecFor(Descriptor.class);

        return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                codec,
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    public static ReactiveDescriptorDao createReactive(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonReactiveClient redissonClient, DescriptorRepository descriptorRepository,
            ReactiveMongoOperations reactiveMongoOperations) {
        Codec codec = RedisCodecFactory.codecFor(Descriptor.class);

        return new BaseReactiveDescriptorDaoImpl(redissonClient, descriptorRepository,
                reactiveMongoOperations,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                codec,
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    private DescriptorDaoFactory() {}
}
