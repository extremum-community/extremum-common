package com.extremum.common.utils;

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

        walk(root, visitor, new Context(), 1);
    }

    private void walk(Object currentTarget, FieldVisitor visitor, Context context, int currentDepth) {
        new InstanceFields(currentTarget.getClass()).stream()
                .forEach(field -> {
                    GetFieldValue lazyValue = new GetFieldValue(field, currentTarget);
                    Object nextValue = lazyValue.get();
                    if (context.alreadySeen(nextValue)) {
                        return;
                    }
                    context.rememberAsSeen(nextValue);
//                    System.out.println(String.format("%s: %s", currentTarget, field.getName()));
                    visitor.visitField(field, lazyValue);
                    if (nextValue != null && nextValue instanceof Iterable) {
                        Iterable<Object> iterable = (Iterable<Object>) nextValue;
                        iterable.forEach(element -> {
                            if (shouldGoDeeper(element, currentDepth)) {
                                walk(element, visitor, context, currentDepth + 1);
                            }
                        });
                    } else if (nextValue != null && nextValue instanceof Object[]) {
                        Object[] array = (Object[]) nextValue;
                        Arrays.asList(array).forEach(element -> {
                            if (shouldGoDeeper(element, currentDepth)) {
                                walk(element, visitor, context, currentDepth + 1);
                            }
                        });
                    } else if (shouldGoDeeper(nextValue, currentDepth)) {
                        walk(nextValue, visitor, context, currentDepth + 1);
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
        private final Map<Object, Object> visitedObjects = new IdentityHashMap<>();

        boolean alreadySeen(Object object) {
            return visitedObjects.containsKey(object);
        }

        void rememberAsSeen(Object object) {
            if (object != null) {
                visitedObjects.put(object, object);
            }
        }
    }
}
