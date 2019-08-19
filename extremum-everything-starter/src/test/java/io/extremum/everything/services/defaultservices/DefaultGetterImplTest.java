package io.extremum.everything.services.defaultservices;

import io.extremum.common.models.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.everything.support.ModelDescriptors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGetterImplTest {
    @InjectMocks
    private DefaultGetterImpl getter;

    @Mock
    private CommonServices commonServices;
    @Mock
    private ModelDescriptors modelDescriptors;

    @Mock
    private CommonService<TestModel> commonService;
    private final TestModel modelFromCommonService = new TestModel();

    @Test
    void whenGetting_thenTheResultIsObtainedViaCommonService() {
        when(modelDescriptors.getModelClassByDescriptorId("internalId"))
                .thenReturn(modelClass(TestModel.class));
        when(commonServices.findServiceByModel(TestModel.class)).thenReturn(commonService);
        when(commonService.get("internalId")).thenReturn(modelFromCommonService);

        Model model = getter.get("internalId");

        assertThat(model, is(sameInstance(modelFromCommonService)));
    }

    @SuppressWarnings("SameParameterValue")
    private Class<Model> modelClass(Class<? extends Model> modelClass) {
        @SuppressWarnings("unchecked") Class<Model> castClass = (Class<Model>) modelClass;
        return castClass;
    }

    @Test
    void whenGettingReactively_thenTheResultIsObtainedViaCommonService() {
        when(modelDescriptors.getModelClassByDescriptorId("internalId"))
                .thenReturn(modelClass(TestModel.class));
        when(commonServices.findServiceByModel(TestModel.class)).thenReturn(commonService);
        when(commonService.reactiveGet("internalId")).thenReturn(Mono.just(modelFromCommonService));

        Model model = getter.reactiveGet("internalId").block();

        assertThat(model, is(sameInstance(modelFromCommonService)));
    }

    private static class TestModel implements Model {
    }
}