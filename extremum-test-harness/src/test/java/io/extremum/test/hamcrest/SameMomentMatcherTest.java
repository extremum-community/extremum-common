package io.extremum.test.hamcrest;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SameMomentMatcherTest {
    @Test
    void matchesDateTimesPointingAtSameMomentButWhichAreNotEqualAccordingToEqualsMethod() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime atUtc = now.withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime atPlus3 = now.withZoneSameInstant(ZoneId.of("+03:00"));

        assertNotEquals(atUtc, atPlus3);

        SameMomentMatcher<Temporal> matcher = SameMomentMatcher.atSameMomentAs(atUtc);

        assertTrue(matcher.matches(atPlus3));
    }
}