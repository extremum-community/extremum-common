package com.extremum.common.deserializers;

import com.extremum.common.exceptions.DeserializationException;
import com.extremum.common.stucts.IntegerRangeOrValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class IntegerRangeOrValueDeserializer extends StdDeserializer<IntegerRangeOrValue> {
    public IntegerRangeOrValueDeserializer() {
        super(IntegerRangeOrValue.class);
    }

    @Override
    public IntegerRangeOrValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);

        if (tree == null) {
            log.warn("Nothing to parse");
            return null;
        }

        if (tree instanceof NumericNode) {
            int intValue = ((NumericNode) tree).intValue();
            return new IntegerRangeOrValue(intValue);
        } else if (tree instanceof ObjectNode) {
            Integer min = null;
            Integer max = null;

            TreeNode minValue = tree.get(IntegerRangeOrValue.FIELDS.min.name());
            TreeNode maxValue = tree.get(IntegerRangeOrValue.FIELDS.max.name());

            if (minValue instanceof NullNode && maxValue instanceof NullNode) {
                return null;
            }

            if (!(minValue instanceof NullNode)) {
                min = ((NumericNode) minValue).intValue();
            }

            if (!(maxValue instanceof NullNode)) {
                max = ((NumericNode) maxValue).intValue();
            }

            return new IntegerRangeOrValue(min, max);
        } else {
            throw new DeserializationException("IntegerRangeOrValue", "Unknown object type: " +
                    tree.getClass().getName());
        }
    }
}
