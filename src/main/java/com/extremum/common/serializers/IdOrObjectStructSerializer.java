package com.extremum.common.serializers;

import com.extremum.common.stucts.IdOrObjectStruct;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IdOrObjectStructSerializer extends StdSerializer<IdOrObjectStruct> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdOrObjectStructSerializer.class);

    private final ObjectMapper mapper;

    public IdOrObjectStructSerializer(ObjectMapper mapper) {
        super(IdOrObjectStruct.class);

        this.mapper = mapper;
    }

    @Override
    public void serialize(IdOrObjectStruct value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            LOGGER.debug("Nothing to serialize, serialization value is null");
            gen.writeNull();
        } else if (value.isComplex()) {
            mapper.writeValue(gen, value.object);
        } else {
            if (value.id != null) {
                gen.writeString(value.id.toString());
            } else {
                LOGGER.debug("Nothing to serialize, ID value is null");
                gen.writeNull();
            }
        }
    }
}
