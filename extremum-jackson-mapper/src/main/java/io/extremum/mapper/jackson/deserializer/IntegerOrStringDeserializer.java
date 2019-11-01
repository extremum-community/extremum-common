package io.extremum.mapper.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.extremum.sharedmodels.basic.IntegerOrString;

import java.io.IOException;

/**
 * @author rpuch
 */
public class IntegerOrStringDeserializer extends StdDeserializer<IntegerOrString> {
    public IntegerOrStringDeserializer() {
        super(IntegerOrString.class);
    }

    @Override
    public IntegerOrString deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        TreeNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node == null) {
            return null;
        }
        if (!node.isValueNode()) {
            throw new IllegalStateException("A value was expected");
        }

        if (!(node instanceof JsonNode)) {
            throw new IllegalStateException("Only JsonNode nodes are supportted, but we got " + node.getClass());
        }
        JsonNode jsonNode = (JsonNode) node;

        if (node.numberType() == JsonParser.NumberType.INT) {
            return new IntegerOrString(jsonNode.intValue());
        }
        if (jsonNode.textValue() != null) {
            return new IntegerOrString(jsonNode.textValue());
        }

        throw new IllegalStateException("Cannot deserialize: it's not empty, not a string and not an integer");
    }
}
