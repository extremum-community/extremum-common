package models.deserialization;

import com.extremum.common.mapper.MockedMapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.sharedmodels.basic.MultilingualLanguage;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringOrMultilingualDeserializerTest {
    private static final String PATH_TO_JSON = "json-files/";
    private ObjectMapper mapper = new SystemJsonObjectMapper(new MockedMapperDependencies());

    @Test
    public void deserializeSimpleTextTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-simple.json");
        StringOrMultilingual ml = mapper.readValue(json, StringOrMultilingual.class);

        assertNotNull(ml);
        assertEquals(StringOrMultilingual.Type.TEXT, ml.getType());
        assertEquals("В лесу родилась ёлочка", ml.getText());
        assertNull(ml.getMultilingual());
    }

    @Test
    public void deserializeComplexObjectTest() throws IOException {
        String json = TestUtils.loadAsStringFromResource(PATH_TO_JSON + "multilingual-text-complex.json");
        StringOrMultilingual ml = mapper.readValue(json, StringOrMultilingual.class);

        assertNotNull(ml);
        assertEquals(StringOrMultilingual.Type.MAP, ml.getType());
        assertNull(ml.getText());
        assertFalse(ml.getMultilingual().getMap().isEmpty());
        assertEquals(2, ml.getMultilingual().getMap().size());

        String ru_RU_text = ml.getMultilingual().getMap().get(MultilingualLanguage.ru_RU);
        assertEquals("В лесу родилась ёлочка", ru_RU_text);

        String en_US_text = ml.getMultilingual().getMap().get(MultilingualLanguage.en_US);
        assertEquals("The forest raised a christmas tree", en_US_text);
    }
}