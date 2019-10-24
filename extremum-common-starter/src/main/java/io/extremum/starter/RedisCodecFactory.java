package io.extremum.starter;

import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

class RedisCodecFactory {
    static Codec codecFor(Class<?> entityClass) {
        return new TypedJsonJacksonCodec(String.class, entityClass,
                new BasicJsonObjectMapper());
    }

    private RedisCodecFactory() {
    }
}
