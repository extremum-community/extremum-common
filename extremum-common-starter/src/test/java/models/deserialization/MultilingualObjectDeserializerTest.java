package models.deserialization;

import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.mapper.MockedMapperDependencies;
import com.extremum.common.stucts.MultilingualLanguage;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MultilingualObjectDeserializerTest {
    private static final String PATH_TO_JSON = "json-files/";
    private ObjectMapper mapper = new SystemJsonObjectMapper(new MockedMapperDependencies());

    @Test
    public void deserializeSimpleTextTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-simple.json");
        MultilingualObject ml = mapper.readValue(json, MultilingualObject.class);

        assertNotNull(ml);
        assertEquals(MultilingualObject.Type.TEXT, ml.type);
        assertEquals("В лесу родилась ёлочка", ml.text);
        assertNull(ml.map);
    }

    @Test
    public void deserializeComplexObjectTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-complex.json");
        MultilingualObject ml = mapper.readValue(json, MultilingualObject.class);

        assertNotNull(ml);
        assertEquals(MultilingualObject.Type.MAP, ml.type);
        assertNull(ml.text);
        assertFalse(ml.map.isEmpty());
        assertEquals(2, ml.map.size());

        String ru_RU_text = ml.map.get(MultilingualLanguage.ru_RU);
        assertEquals("В лесу родилась ёлочка", ru_RU_text);

        String en_US_text = ml.map.get(MultilingualLanguage.en_US);
        assertEquals("The forest raised a christmas tree", en_US_text);
    }
}