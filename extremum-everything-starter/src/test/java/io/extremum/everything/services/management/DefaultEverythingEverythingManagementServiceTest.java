package io.extremum.everything.services.management;

import com.google.common.collect.ImmutableList;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.models.Model;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.GetterService;
import io.extremum.security.AllowEverythingForDataAccess;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingEverythingManagementServiceTest {
    @InjectMocks
    private DefaultEverythingEverythingManagementService service;

    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;

    @BeforeEach
    void setUp() {
        service = new DefaultEverythingEverythingManagementService(
                new ModelRetriever(ImmutableList.of(new AlwaysNullGetterService()), emptyList(), null, null),
                null, null, null,
                dtoConversionService,
                new AllowEverythingForDataAccess()
        );
    }

    @Test
    void givenGetterServiceReturnsNull_whenGetting_thenModelNotFoundExceptionShouldBeThrown() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .modelType("AlwaysNull")
                .build();
        try {
            service.get(descriptor, false);
            fail("A ModelNotFoundException is expected");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is("Nothing was found by 'external-id'"));
        }
    }

    private static class AlwaysNullGetterService implements GetterService<Model> {
        @Override
        public Model get(String id) {
            return null;
        }

        @Override
        public String getSupportedModel() {
            return "AlwaysNull";
        }
    }
}