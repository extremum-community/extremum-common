package com.extremum.common.serializers;

import com.extremum.common.stucts.MultilingualLanguage;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Map;

public class MultilingualObjectSerializer extends StdSerializer<MultilingualObject> {
    public MultilingualObjectSerializer() {
        super(MultilingualObject.class);
    }

    @Override
    public void serialize(MultilingualObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.type == MultilingualObject.Type.TEXT) {
            gen.writeString(value.text);
        } else if (value.type == MultilingualObject.Type.MAP) {
            gen.writeStartObject();

            for (Map.Entry<MultilingualLanguage, String> entry : value.map.entrySet()) {
                gen.writeStringField(entry.getKey().getValue(), entry.getValue());
            }

            gen.writeEndObject();
        } else {
            gen.writeNull();
        }
    }

    @Override
    public void serializeWithType(MultilingualObject value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        serialize(value, gen, serializers);
    }
}
