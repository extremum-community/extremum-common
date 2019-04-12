package com.extremum.common.serializers;

import com.extremum.common.stucts.IdListOrObjectListStruct;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class IdListOrObjectListStructSerializer extends StdSerializer<IdListOrObjectListStruct> {
    public IdListOrObjectListStructSerializer() {
        super(IdListOrObjectListStruct.class);
    }

    @Override
    public void serialize(IdListOrObjectListStruct value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || !value.isContainsIdList()) {
            gen.writeNull();
        } else {
            gen.writeStartArray();

            for (Object id : value.idList) {
                gen.writeString(id.toString());
            }

            gen.writeEndArray();
        }
    }
}
