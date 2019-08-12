package io.extremum.common.mapper;

import io.extremum.common.deserializers.StringOrObjectDeserializers;
import io.extremum.common.deserializers.StringOrObjectTypeModifier;
import io.extremum.common.serializers.StringOrObjectSerializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringOrObjectModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new StringOrObjectSerializers());
        context.addDeserializers(new StringOrObjectDeserializers());
        context.addTypeModifier(new StringOrObjectTypeModifier());
    }
}
