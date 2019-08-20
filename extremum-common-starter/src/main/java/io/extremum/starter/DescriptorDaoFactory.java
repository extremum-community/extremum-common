package io.extremum.starter;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.dao.impl.BaseReactiveDescriptorDaoImpl;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;

public class DescriptorDaoFactory {
    public static DescriptorDao create(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonClient redissonClient, DescriptorRepository descriptorRepository) {
        Codec codec = RedisCodecFactory.codecFor(Descriptor.class);

        if (noRedis(redisProperties)) {
            return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository,
                    descriptorsProperties.getDescriptorsMapName(), descriptorsProperties.getInternalIdsMapName(),
                    codec);
        } else {
            return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository,
                    descriptorsProperties.getDescriptorsMapName(),
                    descriptorsProperties.getInternalIdsMapName(), codec, redisProperties.getCacheSize(),
                    redisProperties.getIdleTime());
        }
    }

    public static ReactiveDescriptorDao createReactive(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonReactiveClient redissonClient, DescriptorRepository descriptorRepository) {
        Codec codec = RedisCodecFactory.codecFor(Descriptor.class);

        if (noRedis(redisProperties)) {
            return new BaseReactiveDescriptorDaoImpl(redissonClient, descriptorRepository,
                    descriptorsProperties.getDescriptorsMapName(), descriptorsProperties.getInternalIdsMapName(),
                    codec);
        } else {
            return new BaseReactiveDescriptorDaoImpl(redissonClient, descriptorRepository,
                    descriptorsProperties.getDescriptorsMapName(),
                    descriptorsProperties.getInternalIdsMapName(), codec, redisProperties.getCacheSize(),
                    redisProperties.getIdleTime());
        }
    }

    private static boolean noRedis(RedisProperties redisProperties) {
        return RedisInitialization.noRedis(redisProperties);
    }

    private DescriptorDaoFactory() {}
}
