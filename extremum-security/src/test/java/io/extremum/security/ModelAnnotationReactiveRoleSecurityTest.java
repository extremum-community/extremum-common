package io.extremum.security;

import io.extremum.common.support.ModelClasses;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ModelAnnotationReactiveRoleSecurityTest {
    @InjectMocks
    private ModelAnnotationReactiveRoleSecurity security;

    @Mock
    private ReactiveRoleChecker roleChecker;
    @Spy
    private ModelClasses modelClasses = new TestModels();

    private final Descriptor secureDescriptor = descriptorForModelName("SecureModel");
    private final Descriptor insecureDescriptor = descriptorForModelName("InsecureModel");
    private final Descriptor emptySecurityDescriptor = descriptorForModelName("EmptySecurityModel");

    private static Descriptor descriptorForModelName(String modelName) {
        return Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .modelType(modelName)
                .build();
    }

    @Test
    void givenCurrentUserHasRoleToGet_whenCheckingWhetherRolesAllowToGet_thenNoExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(true));

        security.checkGetAllowed(secureDescriptor).block();
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToGet_whenCheckingWhetherRolesAllowToGet_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(false));

        try {
            security.checkGetAllowed(secureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToGet_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkGetAllowed(insecureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoGetSecurityAnnotation_whenCheckingWhetherRolesAllowToGet_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkGetAllowed(emptySecurityDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for get operation"));
        }
    }

    @Test
    void givenCurrentUserHasRoleToPatch_whenCheckingWhetherRolesAllowToPatch_thenNoExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(true));

        security.checkPatchAllowed(secureDescriptor).block();
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToPatch_whenCheckingWhetherRolesAllowToPatch_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(false));

        try {
            security.checkPatchAllowed(secureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToPatch_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkPatchAllowed(insecureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoPatchSecurityAnnotation_whenCheckingWhetherRolesAllowToPatch_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkPatchAllowed(emptySecurityDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for patch operation"));
        }
    }

    @Test
    void givenCurrentUserHasRoleToRemove_whenCheckingWhetherRolesAllowToRemove_thenNoExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(true));

        security.checkRemovalAllowed(secureDescriptor).block();
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToRemove_whenCheckingWhetherRolesAllowToRemove_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(false));

        try {
            security.checkRemovalAllowed(secureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToRemove_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkRemovalAllowed(insecureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoRemoveSecurityAnnotation_whenCheckingWhetherRolesAllowToRemove_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkRemovalAllowed(emptySecurityDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for remove operation"));
        }
    }

    @Test
    void givenCurrentUserHasRoleToWatch_whenCheckingWhetherRolesAllowToWatch_thenNoExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(true));

        security.checkWatchAllowed(secureDescriptor).block();
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToWatch_whenCheckingWhetherRolesAllowToWatch_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleChecker.currentUserHasOneRoleOf("ROLE_PRIVILEGED"))
                .thenReturn(Mono.just(false));

        try {
            security.checkWatchAllowed(secureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToWatch_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkWatchAllowed(insecureDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoWatchSecurityAnnotation_whenCheckingWhetherRolesAllowToWatch_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkWatchAllowed(emptySecurityDescriptor).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for watch operation"));
        }
    }

    @ExtremumRequiredRoles(
            defaultAccess = "ROLE_PRIVILEGED"
    )
    private static class SecureModel extends MongoCommonModel {
    }

    private static class InsecureModel extends MongoCommonModel {
    }

    @ExtremumRequiredRoles
    private static class EmptySecurityModel extends MongoCommonModel {
    }

    @SuppressWarnings("unchecked")
    private static class TestModels implements ModelClasses {
        @Override
        public <M extends Model> Class<M> getClassByModelName(String modelName) {
            if ("SecureModel".equals(modelName)) {
                return (Class<M>) SecureModel.class;
            }
            if ("InsecureModel".equals(modelName)) {
                return (Class<M>) InsecureModel.class;
            }
            if ("EmptySecurityModel".equals(modelName)) {
                return (Class<M>) EmptySecurityModel.class;
            }
            throw new IllegalStateException("Should not be here");
        }
    }
}