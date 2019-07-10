package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.security.*;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ModelAnnotationEverythingRoleSecurityTest {
    @InjectMocks
    private ModelAnnotationEverythingRoleSecurity security;

    @Mock
    private RoleBasedSecurity roleBasedSecurity;
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
        when(roleBasedSecurity.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(true);

        security.checkAllowedGet(secureDescriptor);
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToGet_whenCheckingWhetherRolesAllowToGet_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleBasedSecurity.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(false);

        try {
            security.checkAllowedGet(secureDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToGet_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkAllowedGet(insecureDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoGetSecurityAnnotation_whenCheckingWhetherRolesAllowToGet_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkAllowedGet(emptySecurityDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for get operation"));
        }
    }

    @Test
    void givenCurrentUserHasRoleToPatch_whenCheckingWhetherRolesAllowToPatch_thenNoExceptionShouldBeThrown() {
        when(roleBasedSecurity.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(true);

        security.checkAllowedPatch(secureDescriptor);
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToPatch_whenCheckingWhetherRolesAllowToPatch_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleBasedSecurity.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(false);

        try {
            security.checkAllowedPatch(secureDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToPatch_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkAllowedPatch(insecureDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoPatchSecurityAnnotation_whenCheckingWhetherRolesAllowToPatch_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkAllowedPatch(emptySecurityDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for patch operation"));
        }
    }

    @Test
    void givenCurrentUserHasRoleToRemove_whenCheckingWhetherRolesAllowToRemove_thenNoExceptionShouldBeThrown() {
        when(roleBasedSecurity.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(true);

        security.checkAllowedRemove(secureDescriptor);
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToRemove_whenCheckingWhetherRolesAllowToRemove_thenAccessDeniedExceptionShouldBeThrown() {
        when(roleBasedSecurity.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(false);

        try {
            security.checkAllowedRemove(secureDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenModelClassHasNoSecurityAnnotation_whenCheckingWhetherRolesAllowToRemove_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkAllowedRemove(insecureDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'InsecureModel'"));
        }
    }

    @Test
    void givenModelClassHasNoRemoveSecurityAnnotation_whenCheckingWhetherRolesAllowToRemove_thenAccessDeniedExceptionShouldBeThrown() {
        try {
            security.checkAllowedRemove(emptySecurityDescriptor);
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("Security is not configured for 'EmptySecurityModel' for remove operation"));
        }
    }

    @EverythingSecured(
            get = @Access("ROLE_PRIVILEGED"),
            patch = @Access("ROLE_PRIVILEGED"),
            remove = @Access("ROLE_PRIVILEGED")
    )
    private static class SecureModel extends MongoCommonModel {
    }

    private static class InsecureModel extends MongoCommonModel {
    }

    @EverythingSecured
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