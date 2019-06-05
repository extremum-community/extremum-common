package com.extremum.common.utils.attribute;

import com.extremum.common.utils.InstanceFields;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author rpuch
 */
public final class ShallowFieldGraphWalker implements AttributeGraphWalker {
    @Override
    public void walk(Object root, AttributeVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        new InstanceFields(root.getClass()).stream()
                .forEach(field -> visitField(field, root, visitor));
    }

    private void visitField(Field field, Object root, AttributeVisitor visitor) {
        Object value = new GetFieldValue(field, root).get();
        visitor.visitAttribute(new FieldAttribute(field, value));
    }
}
