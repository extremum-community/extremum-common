package com.extremum.everything.security;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.exceptions.EverythingEverythingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
class AccessCheckersDataSecurityTest {
    private AccessCheckersDataSecurity security;

    @BeforeEach
    void createSecurityInstance() {
        security = new AccessCheckersDataSecurity(Arrays.asList(new AllowEverything(), new DenyEverything(),
                new CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation()),
                mock(RoleChecker.class));
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckGet_thenAccessShouldBeAllowed() {
        security.checkGetAllowed(new ModelWithAllowingChecker());
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckGet_thenAccessShouldBeDenied() {
        try {
            security.checkGetAllowed(new ModelWithDenyingChecker());
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckGet_thenAccessShouldBeAllowed() {
        security.checkGetAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation());
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckGet_thenAnExceptionShouldBeThrown() {
        try {
            security.checkGetAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckGet_thenAnExceptionShouldBeThrown() {
        try {
            security.checkGetAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckGet_thenShouldBeAllowed() {
        security.checkGetAllowed(null);
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckPatch_thenAccessShouldBeAllowed() {
        security.checkPatchAllowed(new ModelWithAllowingChecker());
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckPatch_thenAccessShouldBeDenied() {
        try {
            security.checkPatchAllowed(new ModelWithDenyingChecker());
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckPatch_thenAccessShouldBeAllowed() {
        security.checkPatchAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation());
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckPatch_thenAnExceptionShouldBeThrown() {
        try {
            security.checkPatchAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckPatch_thenAnExceptionShouldBeThrown() {
        try {
            security.checkPatchAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckPatch_thenShouldBeAllowed() {
        security.checkPatchAllowed(null);
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckRemove_thenAccessShouldBeAllowed() {
        security.checkRemovalAllowed(new ModelWithAllowingChecker());
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckRemove_thenAccessShouldBeDenied() {
        try {
            security.checkRemovalAllowed(new ModelWithDenyingChecker());
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckRemove_thenAccessShouldBeAllowed() {
        security.checkRemovalAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation());
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckRemove_thenAnExceptionShouldBeThrown() {
        try {
            security.checkRemovalAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckRemove_thenAnExceptionShouldBeThrown() {
        try {
            security.checkRemovalAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckRemove_thenShouldBeAllowed() {
        security.checkRemovalAllowed(null);
    }

    @ModelName("ModelWithAllowingChecker")
    private static class ModelWithAllowingChecker extends MongoCommonModel {
    }

    @ModelName("ModelWithDenyingChecker")
    private static class ModelWithDenyingChecker extends MongoCommonModel {
    }

    @ModelName("ModelWithoutCheckerButWithNoDataSecurityAnnotation")
    @NoDataSecurity
    private static class ModelWithoutCheckerButWithNoDataSecurityAnnotation extends MongoCommonModel {
    }

    @ModelName("ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation")
    private static class ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation extends MongoCommonModel {
    }

    @ModelName("ModelWithCheckerAndWithNoDataSecurityAnnotation")
    @NoDataSecurity
    private static class ModelWithCheckerAndWithNoDataSecurityAnnotation extends MongoCommonModel {
    }

    private static class AllowEverything extends ConstantChecker<ModelWithAllowingChecker> {
        AllowEverything() {
            super(true);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithAllowingChecker";
        }
    }

    private static class DenyEverything extends ConstantChecker<ModelWithDenyingChecker> {
        DenyEverything() {
            super(false);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithDenyingChecker";
        }
    }

    private static class CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation
            extends ConstantChecker<ModelWithCheckerAndWithNoDataSecurityAnnotation> {

        CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation() {
            super(true);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithCheckerAndWithNoDataSecurityAnnotation";
        }
    }
}