package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.mapper.jackson.BasicJsonObjectMapper;
import io.extremum.mapper.util.TestUtils;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class StringOrMultilingualDeserializerTest {
    private static final String PATH_TO_JSON = "json-files/";
    private ObjectMapper mapper = new BasicJsonObjectMapper();

    @Test
    public void deserializeSimpleTextTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-simple.json");
        StringOrMultilingual ml = mapper.readValue(json, StringOrMultilingual.class);

        assertNotNull(ml);
        assertEquals(StringOrMultilingual.Type.TEXT, ml.getType());
        assertEquals("В лесу родилась ёлочка", ml.getText());
        assertNull(ml.getMultilingualContent());
    }

    @Test
    public void deserializeComplexObjectTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-complex.json");
        StringOrMultilingual ml = mapper.readValue(json, StringOrMultilingual.class);

        assertNotNull(ml);
        assertEquals(StringOrMultilingual.Type.MAP, ml.getType());
        assertNull(ml.getText());
        assertFalse(ml.getMultilingualContent().getMap().isEmpty());
        assertEquals(2, ml.getMultilingualContent().getMap().size());

        String ru_RU_text = ml.getMultilingualContent().getMap().get(MultilingualLanguage.ru_RU);
        assertEquals("В лесу родилась ёлочка", ru_RU_text);

        String en_US_text = ml.getMultilingualContent().getMap().get(MultilingualLanguage.en_US);
        assertEquals("The forest raised a christmas tree", en_US_text);
    }
}