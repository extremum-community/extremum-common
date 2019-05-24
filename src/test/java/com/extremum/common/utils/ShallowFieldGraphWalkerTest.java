package com.extremum.common.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
class ShallowFieldGraphWalkerTest {
    private final ShallowFieldGraphWalker walker = new ShallowFieldGraphWalker();
    private final Collector collector = new Collector();

    @Test
    void whenObjectHasNoIntanceField_thenNothingShouldBeVisited() {
        walker.walk(new Object(), collector);

        assertThat(collector.values, hasSize(0));
    }

    @Test
    void whenObjectHasInstanceFields_thenAllOfThemShouldBeVisited() {
        walker.walk(new ShallowBean(), collector);

        assertThat(collector.values, hasSize(4));
        assertThat(collector.values, hasItems("abc", 10L, 20, null));
    }

    @Test
    void whenRootObjectIsNull_thenaNullPointerExceptionShouldBeThrown() {
        try {
            walker.walk(null, collector);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Root cannot be null"));
        }
    }
    
    private static class Collector implements FieldVisitor {
        private final List<Object> values = new ArrayList<>();

        @Override
        public void visitField(Field field, Supplier<Object> lazyValue) {
            values.add(lazyValue.get());
        }
    }
}