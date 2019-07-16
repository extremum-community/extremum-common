package com.extremum.everything.services.management;

import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.security.EverythingRoleSecurity;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class EverythingEverythingManagementServiceRoleSecurityTest {
    private static final boolean DO_NOT_EXPAND = false;

    @InjectMocks
    private RoleSecurityEverythingEverythingManagementService secureService;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType("SecuredEntity")
            .build();

    @Mock
    private EverythingEverythingManagementService insecureService;
    @Mock
    private EverythingRoleSecurity roleSecurity;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final JsonPatch jsonPatch = new JsonPatch(Collections.emptyList());

    @Test
    void givenSecurityRolesAllowGetAnEntity_whenGettingIt_itShouldBeReturned() {
        when(insecureService.get(descriptor, DO_NOT_EXPAND)).thenReturn(responseDto);

        ResponseDto dto = secureService.get(descriptor, DO_NOT_EXPAND);
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowGetAnEntity_whenGettingIt_anExceptionShouldBeThrown() {
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(roleSecurity).checkGetAllowed(descriptor);

        try {
            secureService.get(descriptor, false);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenSecurityRolesAllowPatchAnEntity_whenPatchingIt_itShouldBePatched() {
        when(insecureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND)).thenReturn(responseDto);

        ResponseDto dto = secureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND);
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowPatchAnEntity_whenPatchingIt_anExceptionShouldBeThrown() {
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(roleSecurity).checkPatchAllowed(descriptor);

        try {
            secureService.patch(descriptor, jsonPatch, DO_NOT_EXPAND);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenSecurityRolesAllowRemoveAnEntity_whenRemovingIt_itShouldBeRemoved() {
        secureService.remove(descriptor);
    }

    @Test
    void givenSecurityRolesDoNotAllowRemoveAnEntity_whenRemovingIt_anExceptionShouldBeThrown() {
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(roleSecurity).checkRemovalAllowed(descriptor);

        try {
            secureService.remove(descriptor);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

}
