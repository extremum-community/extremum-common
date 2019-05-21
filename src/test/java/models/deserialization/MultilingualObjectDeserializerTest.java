package models.deserialization;

import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.common.mapper.MockedMapperDependencies;
import com.extremum.common.stucts.Multilingual;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.TestUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MultilingualObjectDeserializerTest {
    private static final String PATH_TO_JSON = "json-files/";
    private ObjectMapper mapper = JsonObjectMapper.createWithCollectionDescriptors(new MockedMapperDependencies());

    @Test
    public void deserializeSimpleTextTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-simple.json");
        MultilingualObject ml = mapper.readValue(json, MultilingualObject.class);

        Assert.assertNotNull(ml);
        Assert.assertEquals(MultilingualObject.Type.TEXT, ml.type);
        Assert.assertEquals("В лесу родилась ёлочка", ml.text);
        Assert.assertNull(ml.map);
    }

    @Test
    public void deserializeComplexObjectTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-complex.json");
        MultilingualObject ml = mapper.readValue(json, MultilingualObject.class);

        Assert.assertNotNull(ml);
        Assert.assertEquals(MultilingualObject.Type.MAP, ml.type);
        Assert.assertNull(ml.text);
        Assert.assertFalse(ml.map.isEmpty());
        Assert.assertEquals(2, ml.map.size());

        String ru_RU_text = ml.map.get(Multilingual.ru_RU);
        Assert.assertEquals("В лесу родилась ёлочка", ru_RU_text);

        String en_US_text = ml.map.get(Multilingual.en_US);
        Assert.assertEquals("The forest raised a christmas tree", en_US_text);
    }
}