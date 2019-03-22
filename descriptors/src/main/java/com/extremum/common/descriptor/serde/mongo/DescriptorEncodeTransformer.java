package com.extremum.common.descriptor.serde.mongo;

import com.extremum.common.descriptor.Descriptor;
import org.bson.Transformer;

public class DescriptorEncodeTransformer implements Transformer {
    @Override
    public Object transform(Object o) {
        return o == null ? null : ((Descriptor) o).getInternalId();
    }
}
