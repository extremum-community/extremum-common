package com.extremum.common.utils.attribute;

import com.extremum.common.utils.InstanceFields;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author rpuch
 */
public class DeepAttributeGraphWalker implements AttributeGraphWalker {
    private static final List<String> PREFIXES_TO_IGNORE = ImmutableList.of("java", "sun.");

    private final int maxLevel;
    private final Predicate<Object> shoudGoDeeperPredicate;

    public DeepAttributeGraphWalker(int maxLevel) {
        this(maxLevel, object -> true);
    }

    public DeepAttributeGraphWalker(int maxLevel, Predicate<Object> shoudGoDeeperPredicate) {
        this.maxLevel = maxLevel;
        this.shoudGoDeeperPredicate = shoudGoDeeperPredicate;
    }

    @Override
    public void walk(Object root, AttributeVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        walkOneLevel(root, new Context(visitor), 1);
    }

    private void walkOneLevel(Object currentTarget, Context context, int currentDepth) {
        new InstanceFields(currentTarget.getClass()).stream()
                .forEach(field -> introspectField(currentTarget, context, currentDepth, field));
    }

    private void introspectField(Object currentTarget, Context context, int currentDepth, Field field) {
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

    private void goDeeperIfNeeded(Object nextValue, Context context, int currentDepth) {
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
        if (currentDepth >= maxLevel) {
            return false;
        }

        if (nextValue == null) {
            return false;
        }

        Class<?> nextClass = nextValue.getClass();

        if (nextClass.getPackage() == null) {
            // something like an array class
            return false;
        }
        if (isJavaSystemClass(nextClass)) {
            return false;
        }

        if (!shoudGoDeeperPredicate.test(nextValue)) {
            return false;
        }
        
        return true;
    }

    private boolean isJavaSystemClass(Class<?> nextClass) {
        return PREFIXES_TO_IGNORE.stream().anyMatch(prefix -> nextClass.getPackage().getName().startsWith(prefix));
    }

    private static class Context {
        private final AttributeVisitor visitor;
        private final Map<Object, Object> visitedObjects = new IdentityHashMap<>();

        private Context(AttributeVisitor visitor) {
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
            visitor.visitAttribute(new FieldAttribute(field, value));
        }
    }
}
