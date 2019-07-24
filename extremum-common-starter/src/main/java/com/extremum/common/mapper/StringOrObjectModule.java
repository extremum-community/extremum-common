package com.extremum.common.mapper;

import com.extremum.common.deserializers.StringOrObjectDeserializers;
import com.extremum.common.deserializers.StringOrObjectTypeModifier;
import com.extremum.common.serializers.StringOrObjectSerializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringOrObjectModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new StringOrObjectSerializers());
        context.addDeserializers(new StringOrObjectDeserializers());
        context.addTypeModifier(new StringOrObjectTypeModifier());
    }
}
