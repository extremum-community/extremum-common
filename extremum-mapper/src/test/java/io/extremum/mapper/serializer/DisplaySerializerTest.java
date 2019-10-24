package io.extremum.mapper.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.BasicJsonObjectMapper;
import io.extremum.mapper.util.TestUtils;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.content.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplaySerializerTest {
    private ObjectMapper mapper = new BasicJsonObjectMapper();

    @Test
    void serializeToSimpleStringTest() throws JsonProcessingException {
        Display display = new Display("test string value");

        String value = mapper.writeValueAsString(display);
        assertEquals("\"test string value\"", value);
    }

    @Test
    void serializeToJsonObjectTest() throws JsonProcessingException, JSONException {
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
                new StringOrMultilingual("caption value"),
                icon,
                splash);

        String value = mapper.writeValueAsString(display);

        String loaded = TestUtils.loadAsStringFromResource("json-files/display-as-object.json");

        JSONObject actual = new JSONObject(value);
        JSONObject expected = new JSONObject(loaded);

        assertEquals(expected.toString(), actual.toString());
    }
}
