package com.extremum.everything;

import com.extremum.everything.collection.Projection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ProjectionCuttingTest {
    private final List<Integer> list = Arrays.asList(1, 2, 3, 4);
    private Projection projection;

    @Test
    void whenNoBoundsAreSpecified_thenCutShouldReturnEverything() {
        projection = Projection.offsetLimit(null, null);

        assertThat(projection.cut(list), is(Arrays.asList(1, 2, 3, 4)));
    }

    @Test
    void whenOnlyOffsetIsSpecified_thenOffsetShouldBeRespected() {
        projection = Projection.offsetLimit(1, null);

        assertThat(projection.cut(list), is(Arrays.asList(2, 3, 4)));
    }

    @Test
    void whenOnlyLimitIsSpecified_thenLimitShouldBeRespected() {
        projection = Projection.offsetLimit(null, 2);

        assertThat(projection.cut(list), is(Arrays.asList(1, 2)));
    }

    @Test
    void whenOffsetAndLimitAreSpecified_thenCutShouldRespectBoth() {
        projection = Projection.offsetLimit(1, 2);

        assertThat(projection.cut(list), is(Arrays.asList(2, 3)));
    }
}
