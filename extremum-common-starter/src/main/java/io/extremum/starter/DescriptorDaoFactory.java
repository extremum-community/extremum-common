package io.extremum.starter;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import io.extremum.common.descriptor.dao.impl.BaseReactiveDescriptorDaoImpl;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

public class DescriptorDaoFactory {
    public static DescriptorDao create(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonClient redissonClient, DescriptorRepository descriptorRepository,
            MongoOperations descriptorMongoOperations) {
        return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository, descriptorMongoOperations,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    public static ReactiveDescriptorDao createReactive(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonReactiveClient redissonClient, DescriptorRepository descriptorRepository,
            ReactiveMongoOperations reactiveMongoOperations) {
        return new BaseReactiveDescriptorDaoImpl(redissonClient, descriptorRepository,
                reactiveMongoOperations,
                descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                redisProperties.getCacheSize(),
                redisProperties.getIdleTime());
    }

    private DescriptorDaoFactory() {}
}
