package com.extremum.common.serializers;

import com.extremum.common.stucts.Display;
import com.extremum.common.stucts.Media;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DisplaySerializer extends StdSerializer<Display> {
    public DisplaySerializer() {
        super(Display.class);
    }

    @Override
    public void serialize(Display display, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (display == null) {
            gen.writeNull();
        } else {
            if (display.isString()) {
                gen.writeString(display.getStringValue());
            } else if (display.isObject()) {
                gen.writeStartObject();

                MultilingualObject caption = display.getCaption();
                if (caption != null) {
                    gen.writeObjectField(Display.FIELDS.caption.name(), caption);
                }

                Media mediaIcon = display.getIcon();
                if (mediaIcon != null) {
                    gen.writeObjectField(Display.FIELDS.icon.name(), mediaIcon);
                }

                Media mediaSplash = display.getSplash();
                if (mediaSplash != null) {
                    gen.writeObjectField(Display.FIELDS.splash.name(), mediaSplash);
                }

                gen.writeEndObject();
            } else {
                log.error("Unknown type {} of Display object", display.getType());
                gen.writeNull();
            }
        }
    }
}
