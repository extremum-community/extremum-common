package io.extremum.starter;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonClient;
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

    private static boolean noRedis(RedisProperties redisProperties) {
        return RedisInitialization.noRedis(redisProperties);
    }

    private DescriptorDaoFactory() {}
}
