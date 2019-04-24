package com.extremum.common.descriptor.serde;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class CollectionDescriptorDeserializer extends StdScalarDeserializer<CollectionDescriptor> {
    public CollectionDescriptorDeserializer() {
        super(CollectionDescriptor.class);
    }

    protected CollectionDescriptorDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected CollectionDescriptorDeserializer(StdScalarDeserializer<?> src) {
        super(src);
    }

    @Override
    public CollectionDescriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String externalId = _parseString(p, ctxt);
        return new CollectionDescriptor(externalId);
    }
}
