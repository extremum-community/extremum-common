package com.extremum.common.descriptor.serde.mongo;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import org.bson.Transformer;

public class DescriptorDecodeTransformer implements Transformer {
    @Override
    public Object transform(Object o) {
        return o == null ? null : DescriptorFactory.fromExternalId(o.toString());
    }
}
