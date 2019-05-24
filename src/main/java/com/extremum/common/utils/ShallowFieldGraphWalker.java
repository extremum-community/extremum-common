package com.extremum.common.utils;

import java.util.Objects;

/**
 * @author rpuch
 */
public final class ShallowFieldGraphWalker implements FieldGraphWalker {
    @Override
    public void walk(Object root, FieldVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        new InstanceFields(root.getClass()).stream()
                .forEach(field -> visitor.visitField(field, new GetFieldValue(field, root)));
    }
}
