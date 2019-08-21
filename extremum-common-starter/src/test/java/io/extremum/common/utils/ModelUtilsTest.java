package io.extremum.common.utils;

import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.HardDelete;
import io.extremum.common.models.annotation.ModelName;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ModelUtilsTest {
    @Test
    void givenModelNameAnnotationExists_whenGetModelNameIsCalledWithClass_thenModelNameShouldBeReturned() {
        MatcherAssert.assertThat(ModelUtils.getModelName(Annotated.class), is("the-name"));
    }

    @Test
    void givenModelNameAnnotationExists_whenGetModelNameIsCalledWithObject_thenModelNameShouldBeReturned() {
        MatcherAssert.assertThat(ModelUtils.getModelName(new Annotated()), is("the-name"));
    }

    @Test
    void givenModelIsAProxyOfAnAnnotatedClass_whenGetModelNameIsCalledWithObject_thenModelNameShouldBeReturned() {
        MatcherAssert.assertThat(ModelUtils.getModelName(new AProxy$HibernateProxy$Tail()), is("the-name"));
    }

    @Test
    void givenNoModelNameAnnotationExists_whenGetModelNameIsCalled_thenAnExceptionShouldBeThrown() {
        try {
            ModelUtils.getModelName(NotAnnotated.class);
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(
                    "class io.extremum.common.utils.ModelUtilsTest$NotAnnotated is not annotated with @ModelName"));
        }
    }

    @Test
    void givenModelNameAnnotationExists_whenHasModelNameIsCalled_thenTrueShouldBeReturned() {
        assertThat(ModelUtils.hasModelName(Annotated.class), is(true));
    }

    @Test
    void givenNoModelNameAnnotationExists_whenHasModelNameIsCalled_thenFalseShouldBeReturned() {
        assertThat(ModelUtils.hasModelName(NotAnnotated.class), is(false));
    }

    @Test
    void givenModelIsAProxyOfAnAnnotatedClass_whenHasModelNameIsCalled_thenModelNameShouldBeReturned() {
        assertThat(ModelUtils.hasModelName(AProxy$HibernateProxy$Tail.class), is(true));
    }

    @Test
    void givenModelIsNotAnnotatedAsHardDelete_whenCheckingWhetherItIsSoftDelete_thenTrueShouldBeReturned() {
        assertThat(ModelUtils.usesSoftDeletion(NotAnnotatedAsHardDelete.class), is(true));
    }

    @Test
    void givenModelIsAnnotatedAsHardDelete_whenCheckingWhetherItIsSoftDelete_thenFalseShouldBeReturned() {
        assertThat(ModelUtils.usesSoftDeletion(AnnotatedAsHardDelete.class), is(false));
    }

    @Test
    void givenModelIsAnnotatedAsHardDelete_whenCheckingWhetherItsProxyIsSoftDelete_thenFalseShouldBeReturned() {
        assertThat(ModelUtils.usesSoftDeletion(AProxyWithHardDelete$HibernateProxy$Tail.class), is(false));
    }

    @ModelName("the-name")
    private static class Annotated extends MongoCommonModel {
    }

    private static class NotAnnotated extends MongoCommonModel {
    }

    private static class AProxy$HibernateProxy$Tail extends Annotated {
    }

    private static class NotAnnotatedAsHardDelete extends MongoCommonModel {
    }

    @HardDelete
    private static class AnnotatedAsHardDelete extends MongoCommonModel {
    }

    private static class AProxyWithHardDelete$HibernateProxy$Tail extends AnnotatedAsHardDelete {
    }
}