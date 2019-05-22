package com.extremum.starter;

import com.extremum.common.mapper.JsonObjectMapper;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

class RedisCodecFactory {
    static Codec codecFor(Class<?> entityClass) {
        return new TypedJsonJacksonCodec(String.class, entityClass,
                JsonObjectMapper.createWithoutDescriptorTransfiguration());
    }

    private RedisCodecFactory() {}
}
