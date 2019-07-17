package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.security.EverythingDataSecurity;
import com.extremum.everything.services.defaultservices.DefaultGetter;
import com.extremum.everything.services.defaultservices.DefaultRemover;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.github.fge.jsonpatch.JsonPatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class EverythingEverythingManagementServiceDataSecurityTest {
    private static final boolean DO_NOT_EXPAND = false;

    private EverythingEverythingManagementService service;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType("SecuredEntity")
            .build();

    @Mock
    private DtoConversionService dtoConversionService;
    @Mock
    private DefaultGetter<Model> defaultGetter;
    @Mock
    private DefaultRemover defaultRemover;
    @Mock
    private EverythingDataSecurity dataSecurity;

    private final JsonPatch jsonPatch = new JsonPatch(emptyList());

    @BeforeEach
    void createService() {
        service = new DefaultEverythingEverythingManagementService(
                emptyList(),
                emptyList(),
                defaultGetter, null, defaultRemover,
                emptyList(), dtoConversionService, null,
                dataSecurity
        );
    }

    private void returnAModelForKnownDescriptor() {
        when(defaultGetter.get("internal-id")).thenReturn(new SecuredEntity());
    }

    @Test
    void givenDataSecurityDoesNotAllowToGet_whenGetting_thenAnExceptionShouldBeThrown() {
        returnAModelForKnownDescriptor();
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(dataSecurity).checkGetAllowed(any());

        try {
            service.get(descriptor, DO_NOT_EXPAND);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    @Disabled("Enable when we split PatcherService to get() and persist()")
    void givenDataSecurityDoesNotAllowToPatch_whenPatching_thenAnExceptionShouldBeThrown() {
        returnAModelForKnownDescriptor();
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(dataSecurity).checkPatchAllowed(any());

        try {
            service.patch(descriptor, jsonPatch, DO_NOT_EXPAND);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenDataSecurityDoesNotAllowToRemove_whenRemoving_thenAnExceptionShouldBeThrown() {
        returnAModelForKnownDescriptor();
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(dataSecurity).checkRemovalAllowed(any());

        try {
            service.remove(descriptor);
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    private static class SecuredEntity extends MongoCommonModel {
    }
}
