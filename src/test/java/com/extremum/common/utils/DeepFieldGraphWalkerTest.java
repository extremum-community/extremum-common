package com.extremum.common.utils;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
class DeepFieldGraphWalkerTest {
    private final DeepFieldGraphWalker walker = new DeepFieldGraphWalker(10);
    private final Collector collector = new Collector();

    @Test
    void whenObjectHasNoIntanceField_thenNothingShouldBeVisited() {
        walker.walk(new Object(), collector);

        assertThat(collector.values, hasSize(0));
    }

    @Test
    void whenObjectHasInstanceFields_thenAllOfThemShouldBeVisited() {
        walker.walk(new ShallowBean(), collector);

        assertThat(collector.values, hasSize(3));
        assertThat(collector.values, hasItems("abc", 10L, 20));
    }

    @Test
    void whenRootObjectIsNull_thenaNullPointerExceptionShouldBeThrown() {
        try {
            walker.walk(null, collector);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Root cannot be null"));
        }
    }

    @Test
    void whenObjectHasEmbeddedObjects_thenAllTheirFieldsShouldBeVisited() {
        DeepBean root = new DeepBean();
        walker.walk(root, collector);

        assertThat(collector.values, hasSize(5));
        assertThat(collector.values, hasItems("I'm deep", root.shallowBean, "abc", 10L, 20));
    }

    @Test
    void whenObjectHasEmbeddedObjectsViaIterable_thenAllTheirFieldsShouldBeVisited() {
        Object embedded1 = new Container("test1");
        Object embedded2 = new Container("test2");
        List<Object> list = Arrays.asList(embedded1, embedded2);

        walker.walk(new Container(list), collector);

        assertThat(collector.values, hasSize(3));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(list, "test1", "test2"))));
    }

    @Test
    void whenObjectHasEmbeddedObjectsViaArray_thenAllTheirFieldsShouldBeVisited() {
        Object embedded1 = new Container("test1");
        Object embedded2 = new Container("test2");
        Object[] array = new Object[]{embedded1, embedded2};

        walker.walk(new Container(array), collector);

        assertThat(collector.values, hasSize(3));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(array, "test1", "test2"))));
    }

    @Test
    void whenObjectHasRecursiveReferences_thenFieldsShouldBeVisitedNormally() {
        Container a = new Container();
        Container b = new Container(a);
        a.object = b;

        walker.walk(new Container(a), collector);

        assertThat(collector.values, hasSize(2));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(a, b))));
    }
    
    @Test
    void whenObjectHasRecursiveReferencesVia_thenFieldsShouldBeVisitedNormally() {
        Container a = new Container();
        Container b = new Container(a);
        a.object = b;

        walker.walk(new Container(a), collector);

        assertThat(collector.values, hasSize(2));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(a, b))));
    }

    @Test
    void givenDepthIsLimited_whenThereAreFieldsDeeperThanTheLimit_thenTheyShouldNotBeVisited() {
        Container c = new Container("test");
        Container b = new Container(c);
        Container a = new Container(b);

        DeepFieldGraphWalker limitedWalker = new DeepFieldGraphWalker(2);

        limitedWalker.walk(new Container(a), collector);

        assertThat(collector.values, hasSize(2));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(a, b))));
    }

    @Test
    void givenPredicateDoesNotAllowToVisitAnything_whenThereAreFieldsToVisit_nothingShouldBeVisited() {
        DeepFieldGraphWalker dontGoDeeper = new DeepFieldGraphWalker(10, object -> false);

        DeepBean root = new DeepBean();
        dontGoDeeper.walk(root, collector);

        assertThat(collector.values, hasSize(2));
        assertThat(collector.values, hasItems("I'm deep", root.shallowBean));
    }

    private static class Collector implements FieldVisitor {
        private final List<Object> values = new ArrayList<>();

        @Override
        public void visitField(Field field, Object value) {
            values.add(value);
        }

        Set<Object> collectedSet() {
            return new HashSet<>(values);
        }
    }

    private static class DeepBean {
        private final String name = "I'm deep";
        private final ShallowBean shallowBean = new ShallowBean();
    }

    private static class Container {
        private Object object;

        private Container() {
        }

        private Container(Object object) {
            this.object = object;
        }
    }
}