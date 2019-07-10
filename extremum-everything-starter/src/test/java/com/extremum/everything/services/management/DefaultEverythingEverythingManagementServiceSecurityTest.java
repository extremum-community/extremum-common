package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.services.defaultservices.DefaultGetter;
import com.extremum.everything.services.defaultservices.DefaultPatcher;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingEverythingManagementServiceSecurityTest {
    private static final boolean DO_NOT_EXPAND = false;

    private EverythingEverythingManagementService service;

    @Mock
    private DtoConversionService dtoConversionService;
    @Mock
    private DefaultGetter<Model> defaultGetter;
    @Mock
    private DefaultPatcher<Model> defaultPatcher;
    @Mock
    private EverythingSecurity security;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType("SecuredEntity")
            .build();
    private final ResponseDto responseDto = mock(ResponseDto.class);

    @BeforeEach
    void createService() {
        service = new DefaultEverythingEverythingManagementService(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                defaultGetter, defaultPatcher, null,
                Collections.emptyList(), dtoConversionService, null,
                security
        );
    }

    @Test
    void givenSecurityRolesAllowGetAnEntity_whenGettingIt_itShouldBeReturned() {
        when(defaultGetter.get("internal-id")).thenReturn(new SecuredEntity());
        when(dtoConversionService.convertUnknownToResponseDto(any(), any())).thenReturn(responseDto);

        ResponseDto dto = service.get(descriptor, DO_NOT_EXPAND);
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowGetAnEntity_whenGettingIt_anExceptionShouldBeThrown() {
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(security).checkRolesAllowCurrentUserToGet(descriptor);

        try {
            service.get(descriptor, false);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenSecurityRolesAllowPatchAnEntity_whenPatchingIt_itShouldBePatched() {
        when(defaultPatcher.patch(eq("internal-id"), any())).thenReturn(new SecuredEntity());
        when(dtoConversionService.convertUnknownToResponseDto(any(), any())).thenReturn(responseDto);

        ResponseDto dto = service.patch(descriptor, new JsonPatch(Collections.emptyList()), DO_NOT_EXPAND);
        assertThat(dto, is(sameInstance(responseDto)));
    }

    @Test
    void givenSecurityRolesDoNotAllowPatchAnEntity_whenPatchingIt_anExceptionShouldBeThrown() {
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(security).checkRolesAllowCurrentUserToPatch(descriptor);

        try {
            service.patch(descriptor, new JsonPatch(Collections.emptyList()), DO_NOT_EXPAND);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @ModelName("SecuredEntity")
    public static class SecuredEntity extends MongoCommonModel {
    }
}
