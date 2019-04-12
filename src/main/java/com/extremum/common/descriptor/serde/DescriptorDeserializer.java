package com.extremum.common.descriptor.serde;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

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
    public Descriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String externalId = _parseString(p, ctxt);
        return DescriptorFactory.fromExternalId(externalId);
    }
}
