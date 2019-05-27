package com.extremum.everything.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author rpuch
 */
class CollectionFragmentTest {
    @Test
    void whenAFragmentIsCreatedEmpty_thenElementsCollectionIsEmptyAndTotalIs0() {
        CollectionFragment<?> fragment = CollectionFragment.emptyWithZeroTotal();

        assertThat(fragment.elements(), hasSize(0));
        assertThat(fragment.total().orElse(1000), is(0L));
    }

    @Test
    void whenAFragmentIsCreatedFromACompleteCollection_thenElementsCollectionIsSameAsOriginalCollection() {
        CollectionFragment<Integer> fragment = CollectionFragment.forCompleteCollection(Arrays.asList(1, 2, 3));

        assertThat(fragment.elements(), is(equalTo(Arrays.asList(1, 2, 3))));
        assertThat(fragment.total().orElse(1000), is(3L));
    }

    @Test
    void whenAFragmentIsCreatedFromAFragment_thenElementsCollectionIsSameAsFragmentsAndTotalIsAsGiven() {
        CollectionFragment<Integer> fragment = CollectionFragment.forFragment(Arrays.asList(1, 2, 3), 10);

        assertThat(fragment.elements(), is(equalTo(Arrays.asList(1, 2, 3))));
        assertThat(fragment.total().orElse(1000), is(10L));
    }

    @Test
    void whenAFragmentIsCreatedEmpty_thenMapMapsToAnEmptyFragment() {
        CollectionFragment<?> fragment = CollectionFragment.emptyWithZeroTotal()
                .map(Function.identity());

        assertThat(fragment.elements(), hasSize(0));
        assertThat(fragment.total().orElse(1000), is(0L));
    }

    @Test
    void whenAFragmentIsCreatedFromACompleteCollection_thenMapMapsCorrectly() {
        CollectionFragment<String> fragment = CollectionFragment.forCompleteCollection(Arrays.asList(1, 2, 3))
                .map(Object::toString);

        assertThat(fragment.elements(), is(equalTo(Arrays.asList("1", "2", "3"))));
        assertThat(fragment.total().orElse(1000), is(3L));
    }

    @Test
    void whenAFragmentIsCreatedFromAFragment_thenMapMapsCorrectly() {
        CollectionFragment<String> fragment = CollectionFragment.forFragment(Arrays.asList(1, 2, 3), 10)
                .map(Object::toString);

        assertThat(fragment.elements(), is(equalTo(Arrays.asList("1", "2", "3"))));
        assertThat(fragment.total().orElse(1000), is(10L));
    }
}