package io.extremum.everything.services.defaultservices;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.models.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.sharedmodels.descriptor.Descriptor;
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
    private UniversalReactiveModelLoaders universalReactiveModelLoaders;
    @Mock
    private ReactiveDescriptorService reactiveDescriptorService;

    @Mock
    private CommonService<TestModel> commonService;

    private final TestModel modelFromDatabase = new TestModel();
    private final Descriptor descriptor = Descriptor.builder()
            .externalId("externalId")
            .internalId("internalId")
            .modelType("TestModel")
            .storageType(Descriptor.StorageType.MONGO)
            .build();

    @Test
    void whenGetting_thenTheResultIsObtainedViaCommonService() {
        when(modelDescriptors.getModelClassByModelInternalId("internalId"))
                .thenReturn(modelClass(TestModel.class));
        when(commonServices.findServiceByModel(TestModel.class)).thenReturn(commonService);
        when(commonService.get("internalId")).thenReturn(modelFromDatabase);

        Model model = getter.get("internalId");

        assertThat(model, is(sameInstance(modelFromDatabase)));
    }

    @SuppressWarnings("SameParameterValue")
    private Class<Model> modelClass(Class<? extends Model> modelClass) {
        @SuppressWarnings("unchecked") Class<Model> castClass = (Class<Model>) modelClass;
        return castClass;
    }

    @Test
    void whenGettingReactively_thenTheResultIsObtainedViaCommonService() {
        when(reactiveDescriptorService.loadByInternalId("internalId"))
                .thenReturn(Mono.just(descriptor));
        when(universalReactiveModelLoaders.loadByDescriptor(descriptor))
                .thenReturn(Mono.just(modelFromDatabase));

        Model model = getter.reactiveGet("internalId").block();

        assertThat(model, is(sameInstance(modelFromDatabase)));
    }

    private static class TestModel implements Model {
    }
}