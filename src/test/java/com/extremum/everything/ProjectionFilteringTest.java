package com.extremum.everything;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.collection.Projection;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ProjectionFilteringTest {
    private static final ZonedDateTime YEAR_2000 = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime YEAR_2005 = YEAR_2000.plusYears(5);
    private static final ZonedDateTime YEAR_2010 = YEAR_2000.plusYears(10);

    private Projection projection = Projection.sinceUntil(YEAR_2000, YEAR_2010);

    @Test
    void whenCreatedIsBetweenSinceAndUntil_thenWeShouldAccept() {
        assertThat(projection.accepts(new TestModel(YEAR_2000)), is(true));
        assertThat(projection.accepts(new TestModel(YEAR_2005)), is(true));
        assertThat(projection.accepts(new TestModel(YEAR_2010)), is(true));
    }

    @Test
    void whenCreatedIsBeforeSince_thenWeShouldNotAccept() {
        assertThat(projection.accepts(new TestModel(YEAR_2000.minusNanos(1))), is(false));
    }

    @Test
    void whenCreatedIsAfterSince_thenWeShouldNotAccept() {
        assertThat(projection.accepts(new TestModel(YEAR_2010.plusNanos(1))), is(false));
    }

    @Test
    void whenCreatedIsNull_thenWeShouldAccept() {
        assertThat(projection.accepts(new TestModel(null)), is(true));
    }

    @Test
    void whenSinceIsNull_thenItShouldWorkAsMinusInfinity() {
        projection = Projection.sinceUntil(null, YEAR_2010);

        assertThat(projection.accepts(new TestModel(YEAR_2005)), is(true));
    }

    @Test
    void whenUntilIsNull_thenItShouldWorkAsPlusInfinity() {
        projection = Projection.sinceUntil(YEAR_2000, null);

        assertThat(projection.accepts(new TestModel(YEAR_2005)), is(true));
    }

    @ModelName("Test")
    private static class TestModel extends MongoCommonModel {
        TestModel(ZonedDateTime created) {
            setCreated(created);
        }
    }
}