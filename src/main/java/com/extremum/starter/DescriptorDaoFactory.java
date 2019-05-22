package com.extremum.starter;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.RedisProperties;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

public class DescriptorDaoFactory {
    public static DescriptorDao create(
            RedisProperties redisProperties, DescriptorsProperties descriptorsProperties,
            RedissonClient redissonClient, DescriptorRepository descriptorRepository) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Descriptor.class,
                JsonObjectMapper.createWithoutDescriptorTransfiguration());

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
