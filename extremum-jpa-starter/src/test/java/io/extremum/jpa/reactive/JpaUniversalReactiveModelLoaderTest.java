package io.extremum.jpa.reactive;

import io.extremum.common.models.Model;
import io.extremum.common.reactive.NaiveReactifier;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaUniversalReactiveModelLoaderTest {
    @InjectMocks
    private JpaUniversalReactiveModelLoader loader;

    @Mock
    private CommonServices commonServices;

    @Mock
    private CommonService<TestModel> commonService;
    @Spy
    private Reactifier reactifier = new NaiveReactifier();

    private final TestModel model = new TestModel();

    @Test
    void whenLoadingAModel_thenItShouldBeLoadedViaACommonService() {
        when(commonServices.findServiceByModel(TestModel.class)).thenReturn(commonService);
        when(commonService.get("internalId")).thenReturn(model);

        Mono<Model> modelMono = loader.loadByInternalId("internalId", TestModel.class);

        assertThat(modelMono.block(), is(sameInstance(model)));
    }

    private static class TestModel implements Model {
    }
}