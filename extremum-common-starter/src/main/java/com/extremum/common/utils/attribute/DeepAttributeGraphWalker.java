package com.extremum.common.utils.attribute;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.function.Predicate;

/**
 * Deep attribute graph walker. Considers both fields and properties,
 * proceeds to their attributes as well. The maximum depth is controllable
 * via constructor parameters, as well as a rule that defines whether
 * the walker should go 'inside' current object.
 *
 * @author rpuch
 */
public class DeepAttributeGraphWalker implements AttributeGraphWalker {
    private static final List<String> PREFIXES_TO_IGNORE = ImmutableList.of("java", "sun.");
    private static final int INITIAL_DEPTH = 1;

    private final int maxLevel;
    private final Predicate<Object> shouldGoDeeperPredicate;

    public DeepAttributeGraphWalker(int maxLevel) {
        this(maxLevel, object -> true);
    }

    public DeepAttributeGraphWalker(int maxLevel, Predicate<Object> shouldGoDeeperPredicate) {
        this.maxLevel = maxLevel;
        this.shouldGoDeeperPredicate = shouldGoDeeperPredicate;
    }

    @Override
    public void walk(Object root, AttributeVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        walkRecursively(root, new Context(visitor), INITIAL_DEPTH);
    }

    private void walkRecursively(Object currentTarget, Context context, int currentDepth) {
        new InstanceAttributes(currentTarget).stream()
                .forEach(attribute -> introspectAttribute(context, currentDepth, attribute));
    }

    private void introspectAttribute(Context context, int currentDepth, Attribute attribute) {
        Object attributeValue = attribute.value();
        if (attributeValue == null) {
            return;
        }

        if (context.alreadySeen(attributeValue)) {
            return;
        }
        context.rememberAsSeen(attributeValue);

        context.visitAttribute(attribute);

        goDeeperIfNeeded(attributeValue, context, currentDepth);
    }

    private void goDeeperIfNeeded(Object nextValue, Context context, int currentDepth) {
        if (nextValue instanceof Iterable) {
            @SuppressWarnings("unchecked") Iterable<Object> iterable = (Iterable<Object>) nextValue;
            goDeeperThroughIterable(iterable, context, currentDepth);
        } else if (nextValue instanceof Object[]) {
            Object[] array = (Object[]) nextValue;
            goDeeperThroughIterable(Arrays.asList(array), context, currentDepth);
        } else if (shouldGoDeeper(nextValue, currentDepth)) {
            walkRecursively(nextValue, context, currentDepth + 1);
        }
    }

    private void goDeeperThroughIterable(Iterable<Object> iterable,
            Context context, int currentDepth) {
        iterable.forEach(element -> {
            if (shouldGoDeeper(element, currentDepth)) {
                walkRecursively(element, context, currentDepth + 1);
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

        if (!shouldGoDeeperPredicate.test(nextValue)) {
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

        void visitAttribute(Attribute attribute) {
            visitor.visitAttribute(attribute);
        }
    }
}
