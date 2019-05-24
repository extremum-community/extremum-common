package com.extremum.common.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rpuch
 */
public final class DeepFieldGraphWalker implements FieldGraphWalker {
    private final int maxDepth;

    public DeepFieldGraphWalker() {
        this(10);
    }

    public DeepFieldGraphWalker(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void walk(Object root, FieldVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        walkOneLevel(root, new Context(visitor), 1);
    }

    private void walkOneLevel(Object currentTarget, Context context, int currentDepth) {
        new InstanceFields(currentTarget.getClass()).stream()
                .forEach(field -> introspectField(currentTarget, context, currentDepth, field));
    }

    private void introspectField(Object currentTarget, Context context,
            int currentDepth, Field field) {
        Object fieldValue = getFieldValue(currentTarget, field);
        if (fieldValue == null) {
            return;
        }

        if (context.alreadySeen(fieldValue)) {
            return;
        }
        context.rememberAsSeen(fieldValue);

        context.visitField(field, fieldValue);

        goDeeperIfNeeded(fieldValue, context, currentDepth);
    }

    private Object getFieldValue(Object currentTarget, Field field) {
        return new GetFieldValue(field, currentTarget).get();
    }

    private void goDeeperIfNeeded(Object nextValue, Context context,
            int currentDepth) {
        if (nextValue instanceof Iterable) {
            @SuppressWarnings("unchecked") Iterable<Object> iterable = (Iterable<Object>) nextValue;
            goDeeperThroughIterable(iterable, context, currentDepth);
        } else if (nextValue instanceof Object[]) {
            Object[] array = (Object[]) nextValue;
            goDeeperThroughIterable(Arrays.asList(array), context, currentDepth);
        } else if (shouldGoDeeper(nextValue, currentDepth)) {
            walkOneLevel(nextValue, context, currentDepth + 1);
        }
    }

    private void goDeeperThroughIterable(Iterable<Object> iterable,
            Context context, int currentDepth) {
        iterable.forEach(element -> {
            if (shouldGoDeeper(element, currentDepth)) {
                walkOneLevel(element, context, currentDepth + 1);
            }
        });
    }

    private boolean shouldGoDeeper(Object nextValue, int currentDepth) {
        if (currentDepth >= maxDepth) {
            return false;
        }

        if (nextValue == null) {
            return false;
        }

        Class<?> nextClass = nextValue.getClass();

        if (nextClass.getPackage() == null) {
            return false;
        }
        if (nextClass.getPackage().getName().startsWith("java")) {
            return false;
        }
        
        return true;
    }

    private static class Context {
        private final FieldVisitor visitor;
        private final Map<Object, Object> visitedObjects = new IdentityHashMap<>();

        private Context(FieldVisitor visitor) {
            this.visitor = visitor;
        }

        boolean alreadySeen(Object object) {
            return visitedObjects.containsKey(object);
        }

        void rememberAsSeen(Object object) {
            if (object != null) {
                visitedObjects.put(object, object);
            }
        }

        void visitField(Field field, Object value) {
            visitor.visitField(field, value);
        }
    }
}
