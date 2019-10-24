package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.mapper.jackson.exceptions.DeserializationException;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StringOrMultilingualDeserializer extends StdDeserializer<StringOrMultilingual> {
    public StringOrMultilingualDeserializer() {
        super(StringOrMultilingual.class);
    }

    @Override
    public StringOrMultilingual deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);

        if (tree instanceof TextNode) {
            return new StringOrMultilingual(((TextNode) tree).textValue());
        }

        if (tree instanceof ObjectNode) {
            String nodeTextValue = tree.toString();
            JSONObject json;
            try {
                json = new JSONObject(nodeTextValue);
            } catch (JSONException e) {
                throw new RuntimeException("Can't parse json " + nodeTextValue);
            }

            Map<String, String> errors = new HashMap<>();
            Map<MultilingualLanguage, String> multilingualMap = new HashMap<>();

            Iterator keysIterator = json.keys();
            while (keysIterator.hasNext()) {
                String key = (String) keysIterator.next();
                MultilingualLanguage multilingual = MultilingualLanguage.fromString(key);

                if (multilingual == null) {
                    errors.put(key, "Invalid language. Use RFC 5646");
                }

                try {
                    multilingualMap.put(multilingual, json.getString(key));
                } catch (JSONException e) {
                    throw new RuntimeException("Can't get a property " + key + " from json " + nodeTextValue);
                }
            }

            if (!errors.isEmpty()) {
                throw new DeserializationException(errors);
            }

            return new StringOrMultilingual(multilingualMap);
        } else {
            throw new DeserializationException("StringOrMultilingual", "must be in a simple text format of multilingual object");
        }
    }
}
