package com.extremum.common.stucts;

import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.common.mapper.MockedMapperDependencies;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.TestUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisplaySerializeTest {
    private ObjectMapper mapper = JsonObjectMapper.createWithCollectionDescriptors(new MockedMapperDependencies());

    @Test
    public void serializeToSimpleStringTest() throws JsonProcessingException {
        Display display = new Display("test string value");

        String value = mapper.writeValueAsString(display);
        assertEquals("\"test string value\"", value);
    }

    @Test
    public void serializeToJsonObjectTest() throws JsonProcessingException, JSONException {
        Media icon = new Media();
        icon.setUrl("/url/to/resource");
        icon.setType(MediaType.IMAGE);
        icon.setWidth(100);
        icon.setHeight(200);
        icon.setDepth(2);
        icon.setDuration(new IntegerOrString(20));


        Media thumbnails = new Media();
        thumbnails.setUrl("/url/to/resource2");
        thumbnails.setType(MediaType.IMAGE);
        thumbnails.setWidth(200);
        thumbnails.setHeight(300);
        thumbnails.setDepth(4);
        thumbnails.setDuration(new IntegerOrString("20"));

        icon.setThumbnails(Collections.singletonList(thumbnails));

        Media splash = new Media();
        splash.setUrl("/url/to/resource3");
        splash.setType(MediaType.IMAGE);
        splash.setWidth(200);
        splash.setHeight(300);
        splash.setDepth(4);
        splash.setDuration(new IntegerOrString(10));

        Display display = new Display(
                new MultilingualObject("caption value"),
                icon,
                splash);

        String value = mapper.writeValueAsString(display);

        String loaded = TestUtils.loadAsStringFromResource("json-files/display-as-object.json");

        JSONObject actual = new JSONObject(value);
        JSONObject expected = new JSONObject(loaded);

        assertEquals(expected.toString(), actual.toString());
    }
}
