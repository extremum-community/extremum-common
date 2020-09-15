package io.extremum.descriptors.sync.dao;

import io.extremum.descriptors.common.dao.DescriptorRepository;
import io.extremum.descriptors.common.properties.DescriptorsProperties;
import io.extremum.descriptors.common.properties.RedisProperties;
import io.extremum.descriptors.sync.dao.DescriptorDao;
import io.extremum.descriptors.sync.dao.impl.BaseDescriptorDaoImpl;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoOperations;

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

    private DescriptorDaoFactory() {}
}
