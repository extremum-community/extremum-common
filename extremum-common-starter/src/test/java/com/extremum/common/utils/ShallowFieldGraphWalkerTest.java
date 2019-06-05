package com.extremum.common.utils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
class ShallowFieldGraphWalkerTest {
    private final ShallowFieldGraphWalker walker = new ShallowFieldGraphWalker();
    private final ValueCollector collector = new ValueCollector();

    @Test
    void whenObjectHasNoIntanceField_thenNothingShouldBeVisited() {
        walker.walk(new Object(), collector);

        assertThat(collector.getValues(), hasSize(0));
    }

    @Test
    void whenObjectHasInstanceFields_thenAllOfThemShouldBeVisited() {
        walker.walk(new ShallowBean(), collector);

        assertThat(collector.getValues(), hasSize(4));
        assertThat(collector.getValues(), hasItems("abc", 10L, 20, null));
    }

    @Test
    void whenRootObjectIsNull_thenaNullPointerExceptionShouldBeThrown() {
        try {
            walker.walk(null, collector);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Root cannot be null"));
        }
    }

}