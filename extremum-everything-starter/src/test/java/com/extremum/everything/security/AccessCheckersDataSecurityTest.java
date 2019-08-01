package com.extremum.everything.security;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.exceptions.EverythingEverythingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class AccessCheckersDataSecurityTest {
    private static final String ROLE_PRIVILEGED = "ROLE_PRIVILEGED";

    private AccessCheckersDataSecurity security;

    @Mock
    private RoleChecker roleChecker;
    @Mock
    private PrincipalSource principalSource;

    @BeforeEach
    void createSecurityInstance() {
        security = new AccessCheckersDataSecurity(Arrays.asList(new AllowEverything(), new DenyEverything(),
                new CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation(),
                new CheckerForModelWithRoleChecksInContext(),
                new CheckerForModelWithPrincipalChecksInContext()),
                roleChecker, principalSource);
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

    @Test
    void givenRoleCheckerAllowsPrivilegedRole_whenCheckerChecksForPrivilegedRoleViaContext_thenItShouldBeAllowed() {
        when(roleChecker.currentUserHasOneRoleOf(ROLE_PRIVILEGED)).thenReturn(true);

        security.checkGetAllowed(new ModelWithRoleChecksInContext());
    }

    @Test
    void givenRoleCheckerDeniesPrivilegedRole_whenCheckerChecksForPrivilegedRoleViaContext_thenItShouldBeDenied() {
        when(roleChecker.currentUserHasOneRoleOf(ROLE_PRIVILEGED)).thenReturn(false);

        assertThrows(EverythingAccessDeniedException.class,
                () -> security.checkGetAllowed(new ModelWithRoleChecksInContext()));
    }

    @Test
    void givenModelOwnerIsAlexAndCurrentPrincipalIsAlex_whenCheckerChecksOwnerMatchesPrincipal_thenItShouldBeAllowed() {
        when(principalSource.getPrincipal()).thenReturn("Alex");

        security.checkGetAllowed(new ModelWithPrincipalChecksInContext());
    }

    @Test
    void givenModelOwnerIsAlexAndCurrentPrincipalIsBen_whenCheckerChecksOwnerMatchesPrincipal_thenItShouldBeDenied() {
        when(principalSource.getPrincipal()).thenReturn("Ben");

        assertThrows(EverythingAccessDeniedException.class,
                () -> security.checkGetAllowed(new ModelWithPrincipalChecksInContext()));
    }

    private static abstract class BaseModel extends MongoCommonModel {
    }

    @ModelName("ModelWithAllowingChecker")
    private static class ModelWithAllowingChecker extends BaseModel {
    }

    @ModelName("ModelWithDenyingChecker")
    private static class ModelWithDenyingChecker extends BaseModel {
    }

    @ModelName("ModelWithoutCheckerButWithNoDataSecurityAnnotation")
    @NoDataSecurity
    private static class ModelWithoutCheckerButWithNoDataSecurityAnnotation extends BaseModel {
    }

    @ModelName("ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation")
    private static class ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation extends BaseModel {
    }

    @ModelName("ModelWithCheckerAndWithNoDataSecurityAnnotation")
    @NoDataSecurity
    private static class ModelWithCheckerAndWithNoDataSecurityAnnotation extends BaseModel {
    }

    @ModelName("ModelWithRoleChecksInContext")
    private static class ModelWithRoleChecksInContext extends BaseModel {
    }

    @ModelName("ModelWithPrincipalChecksInContext")
    private static class ModelWithPrincipalChecksInContext extends BaseModel {
        private final String owner = "Alex";
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

    private static class CheckerForModelWithRoleChecksInContext
            extends SamePolicyChecker<ModelWithRoleChecksInContext> {

        @Override
        boolean allowed(ModelWithRoleChecksInContext model, CheckerContext context) {
            return context.currentUserHasOneOf(ROLE_PRIVILEGED);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithRoleChecksInContext";
        }
    }

    private static class CheckerForModelWithPrincipalChecksInContext
            extends SamePolicyChecker<ModelWithPrincipalChecksInContext> {

        @Override
        boolean allowed(ModelWithPrincipalChecksInContext model, CheckerContext context) {
            return Objects.equals(model.owner, context.getCurrentPrincipal());
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithPrincipalChecksInContext";
        }
    }
}