package io.extremum.common.descriptor.dao.impl;

import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

public class DescriptorCodecs {
    public static Codec codecForDescriptor() {
        return new TypedJsonJacksonCodec(String.class, Descriptor.class,
                new BasicJsonObjectMapper());
    }

    private DescriptorCodecs() {
    }
}
