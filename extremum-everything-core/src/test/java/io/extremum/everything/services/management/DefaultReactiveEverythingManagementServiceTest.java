package io.extremum.everything.services.management;

import com.google.common.collect.ImmutableList;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.ReactiveGetterService;
import io.extremum.security.AllowEverythingForDataAccessReactively;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultReactiveEverythingManagementServiceTest {
    @InjectMocks
    private DefaultReactiveEverythingManagementService service;

    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;

    @BeforeEach
    void setUp() {
        service = new DefaultReactiveEverythingManagementService(
                new ModelRetriever(emptyList(), ImmutableList.of(new AlwaysEmptyGetterService()), null, null),
                null, null, null,
                dtoConversionService,
                new AllowEverythingForDataAccessReactively()
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
            service.get(descriptor, false).block();
            fail("A ModelNotFoundException is expected");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is("Nothing was found by 'external-id'"));
        }
    }

    private static class AlwaysEmptyGetterService implements ReactiveGetterService<Model> {
        @Override
        public Mono<Model> get(String id) {
            return Mono.empty();
        }

        @Override
        public String getSupportedModel() {
            return "AlwaysNull";
        }
    }
}