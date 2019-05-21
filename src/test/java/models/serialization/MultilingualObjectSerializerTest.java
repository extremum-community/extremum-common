package models.serialization;

import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.common.mapper.MockedMapperDependencies;
import com.extremum.common.stucts.Multilingual;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MultilingualObjectSerializerTest {
    private ObjectMapper mapper = JsonObjectMapper.createWithCollectionDescriptors(new MockedMapperDependencies());

    @Test
    public void serializeToSimpleTextTest() throws JsonProcessingException {
        MultilingualObject ml = new MultilingualObject("В лесу родилась ёлочка");
        String json = mapper.writeValueAsString(ml);
        Assert.assertNotNull(json);
        Assert.assertEquals("\"В лесу родилась ёлочка\"", json);
    }

    @Test
    public void serializeToComplexObjectTest() throws JsonProcessingException, JSONException {
        String expectedText_ru = "В лесу родилась ёлочка";
        String expectedLang_ru = "ru-RU";
        String expectedText_en = "The forest raised a christmas tree";
        String expectedLang_en = "en-US";

        Map<Multilingual, String> map = new HashMap<>();
        map.put(Multilingual.ru_RU, expectedText_ru);
        map.put(Multilingual.en_US, expectedText_en);

        MultilingualObject ml = new MultilingualObject(map);
        String json = mapper.writeValueAsString(ml);

        JSONObject jsonObject = new JSONObject(json);
        Assert.assertTrue(jsonObject.has(expectedLang_ru));
        Assert.assertTrue(jsonObject.has(expectedLang_en));
        Assert.assertEquals(expectedText_ru, jsonObject.getString(expectedLang_ru));
        Assert.assertEquals(expectedText_en, jsonObject.getString(expectedLang_en));
    }

    @Test
    public void serializeNullTest() throws JsonProcessingException {
        MultilingualObject ml = new MultilingualObject();
        String json = mapper.writeValueAsString(ml);
        Assert.assertEquals("null", json);
    }
}