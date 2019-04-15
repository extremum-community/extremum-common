package com.extremum.common.descriptor.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;

import java.io.IOException;

public class DescriptorDeserializer extends StdScalarDeserializer<Descriptor> {
    public DescriptorDeserializer() {
        super(Descriptor.class);
    }

    protected DescriptorDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected DescriptorDeserializer(StdScalarDeserializer<?> src) {
        super(src);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Descriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String externalId = _parseString(p, ctxt);
        return DescriptorFactory.fromExternalId(externalId);
    }
}
