package io.extremum.common.utils.annotation;

import io.extremum.common.utils.attribute.Attribute;
import io.extremum.common.utils.attribute.AttributeVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rpuch
 */
class ValueCollector implements AttributeVisitor {
    private final List<Object> values = new ArrayList<>();

    List<Object> getValues() {
        return values;
    }

    Set<Object> collectedSet() {
        return new HashSet<>(values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        values.add(attribute.value());
    }
}
