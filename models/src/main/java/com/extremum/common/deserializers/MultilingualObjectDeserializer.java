package com.extremum.common.deserializers;

import com.extremum.common.exceptions.DeserializationException;
import com.extremum.common.stucts.Multilingual;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultilingualObjectDeserializer extends StdDeserializer<MultilingualObject> {
    public MultilingualObjectDeserializer() {
        super(MultilingualObject.class);
    }

    @Override
    public MultilingualObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TreeNode tree = p.getCodec().readTree(p);

        MultilingualObject model = new MultilingualObject();

        if (tree instanceof TextNode) {
            model = new MultilingualObject(((TextNode) tree).textValue());
        } else if (tree instanceof ObjectNode) {
            String nodeTextValue = tree.toString();
            JSONObject json;
            try {
                json = new JSONObject(nodeTextValue);
            } catch (JSONException e) {
                throw new RuntimeException("Can't parse json " + nodeTextValue);
            }

            Map<String, String> errors = new HashMap<>();
            Map<Multilingual, String> multilingualMap = new HashMap<>();

            Iterator keysIterator = json.keys();
            while (keysIterator.hasNext()) {
                String key = (String) keysIterator.next();
                Multilingual multilingual = Multilingual.fromString(key);

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

            model = new MultilingualObject(multilingualMap);
        } else {
            throw new DeserializationException("MultilingualObject", "must be in a simple text format of multilingual object");
        }

        return model;
    }
}
