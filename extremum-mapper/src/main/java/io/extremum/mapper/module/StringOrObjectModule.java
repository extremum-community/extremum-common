package io.extremum.mapper.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.extremum.mapper.deserializer.StringOrObjectDeserializers;
import io.extremum.mapper.modifier.StringOrObjectTypeModifier;
import io.extremum.mapper.serializer.StringOrObjectSerializers;

public class StringOrObjectModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new StringOrObjectSerializers());
        context.addDeserializers(new StringOrObjectDeserializers());
        context.addTypeModifier(new StringOrObjectTypeModifier());
    }
}
