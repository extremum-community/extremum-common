package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.security.Access;
import com.extremum.everything.security.AccessChecker;
import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.security.EverythingSecured;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
class EverythingSecurityImplTest {
    @InjectMocks
    private EverythingSecurityImpl security;

    @Mock
    private AccessChecker accessChecker;
    @Spy
    private ModelClasses modelClasses = new ModelClasses() {
        @Override
        public <M extends Model> Class<M> getClassByModelName(String modelName) {
            return (Class<M>) SecureModel.class;
        }
    };

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType("SecureModel")
            .build();

    @Test
    void givenCurrentUserHasRoleToGet_whenCheckingWhetherRolesAllowToGet_thenNoExceptionShouldBeThrown() {
        when(accessChecker.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(true);

        security.checkRolesAllowCurrentUserToGet(descriptor);
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleToGet_whenCheckingWhetherRolesAllowToGet_thenExceptionShouldBeThrown() {
        when(accessChecker.currentUserHasOneOf("ROLE_PRIVILEGED")).thenReturn(false);

        try {
            security.checkRolesAllowCurrentUserToGet(descriptor);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @EverythingSecured(
            get = @Access("ROLE_PRIVILEGED")
    )
    public static class SecureModel extends MongoCommonModel {

    }
}