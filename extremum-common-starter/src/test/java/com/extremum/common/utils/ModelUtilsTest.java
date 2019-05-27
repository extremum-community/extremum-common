package com.extremum.common.utils;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
public class ModelUtilsTest {
    @Test
    public void givenModelNameAnnotationExists_whenGetModelNameIsCalledWithClass_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.getModelName(Annotated.class), is("the-name"));
    }

    @Test
    public void givenModelNameAnnotationExists_whenGetModelNameIsCalledWithObject_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.getModelName(new Annotated()), is("the-name"));
    }

    @Test
    public void givenNoModelNameAnnotationExists_whenGetModelNameIsCalled_thenAnExceptionShouldBeThrown() {
        try {
            ModelUtils.getModelName(NotAnnotated.class);
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(
                    "class com.extremum.common.utils.ModelUtilsTest$NotAnnotated is not annotated with @ModelName"));
        }
    }

    @ModelName("the-name")
    private static class Annotated extends MongoCommonModel {
    }

    private static class NotAnnotated extends MongoCommonModel {
    }
}