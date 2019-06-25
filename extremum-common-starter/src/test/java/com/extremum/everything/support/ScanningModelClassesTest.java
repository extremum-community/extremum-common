package com.extremum.everything.support;

import com.extremum.common.models.Model;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ScanningModelClassesTest {
    private final ModelClasses modelClasses = new ScanningModelClasses(
            ImmutableList.of("com.extremum.everything.support"));

    @Test
    void givenAnEntityClassIsInAScannedPackage_whenGettingClassByModelName_thenItShouldBeFound() {
        Class<? extends Model> modelClass = modelClasses.getClassByModelName("FirstModel");

        assertThat(modelClass, is(sameInstance(FirstModel.class)));
    }

    @Test
    void givenAnEntityClassIsNotInAScannedPackage_whenGettingClassByModelName_thenAnExceptionShouldBeThrown() {
        try {
            modelClasses.getClassByModelName("NonExistingModel");
            fail("An exception should be thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(
                    "Model with name NonExistingModel is not known, probably it doesn't have @ModelName annotation"));
        }
    }
}