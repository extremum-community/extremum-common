package com.extremum.common.stucts;

import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.mapper.MockedMapperDependencies;
import models.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DisplayDeserializeTest {
    private SystemJsonObjectMapper mapper = new SystemJsonObjectMapper(new MockedMapperDependencies());

    @Test
    public void deserializeFromSimpleStringTest() throws IOException {
        InputStream is = TestUtils.loadFromResources("json-files/display-as-string.json");
        Display display = mapper.readValue(is, Display.class);

        assertNotNull(display);
        assertTrue(display.isString());
        assertEquals("string value", display.getStringValue());
    }

    @Test
    public void deserializeFromRawJsonObjectStringTest() throws IOException {
        InputStream is = TestUtils.loadFromResources("json-files/display-as-object.json");
        Display display = mapper.readValue(is, Display.class);

        assertNotNull(display);
        assertTrue(display.isObject());
        Media icon = display.getIcon();
        assertNotNull(icon);

        assertEquals("/url/to/resource", icon.getUrl());
        assertEquals(MediaType.IMAGE, icon.getType());
        assertEquals(100, (int) icon.getWidth());
        assertEquals(200, (int) icon.getHeight());
        assertEquals(2, (int) icon.getDepth());
        assertNotNull(icon.getDuration());
        assertTrue(icon.getDuration().isInteger());
        assertEquals(20, (int) icon.getDuration().getIntegerValue());

        List<Media> thumbnails = icon.getThumbnails();
        assertNotNull(thumbnails);
        assertEquals(1, thumbnails.size());
        Media thumbnail = thumbnails.get(0);

        assertEquals("/url/to/resource2", thumbnail.getUrl());
        assertEquals(MediaType.IMAGE, thumbnail.getType());
        assertEquals(200, (int) thumbnail.getWidth());
        assertEquals(300, (int) thumbnail.getHeight());
        assertEquals(4, (int) thumbnail.getDepth());
        assertNotNull(thumbnail.getDuration());
        assertTrue(thumbnail.getDuration().isString());
        assertEquals("20", thumbnail.getDuration().getStringValue());

        Media splash = display.getSplash();
        assertNotNull(splash);

        assertEquals("/url/to/resource3", splash.getUrl());
        assertEquals(MediaType.IMAGE, splash.getType());
        assertEquals(200, (int) splash.getWidth());
        assertEquals(300, (int) splash.getHeight());
        assertEquals(4, (int) splash.getDepth());
        assertNotNull(splash.getDuration());
        assertTrue(splash.getDuration().isInteger());
        assertEquals(10, (int) splash.getDuration().getIntegerValue());
    }
}
