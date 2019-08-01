package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.StubDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.services.DefaultDtoConversionService;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.dto.converters.services.DtoConvertersCollection;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.MockedMapperDependencies;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.security.AllowEverythingForDataAccess;
import com.extremum.security.ExtremumAccessDeniedException;
import com.extremum.security.DataSecurity;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.RequestDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class PatchFlowImplTest {
    private static final String BEFORE_PATCHING = "Before patching";
    private static final String AFTER_PATCHING = "After patching";

    private PatchFlowImpl patchFlow;

    @Mock
    private ModelRetriever modelRetriever;
    @Mock
    private ModelSaver modelSaver;
    @Spy
    private DtoConversionService dtoConversionService = new DefaultDtoConversionService(
            new DtoConvertersCollection(
                    singletonList(new TestModelDtoConverter()),
                    singletonList(new TestModelDtoConverter()),
                    emptyList()
            ),
            new StubDtoConverter()
    );
    @Spy
    private ObjectMapper objectMapper = new SystemJsonObjectMapper(new MockedMapperDependencies());
    @Spy
    private EmptyFieldDestroyer emptyFieldDestroyer = new PublicEmptyFieldDestroyer();
    @Mock
    private RequestDtoValidator requestDtoValidator;
    @Spy
    private DataSecurity dataSecurity = new AllowEverythingForDataAccess();
    @Spy
    private PatcherHooksCollection patcherHooksCollection = new PatcherHooksCollection(emptyList());

    @Captor
    private ArgumentCaptor<TestModel> testModelCaptor;

    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .modelType(TestModel.MODEL_NAME)
            .storageType(Descriptor.StorageType.MONGO)
            .build();

    @BeforeEach
    void createPatcherFlow() {
        Patcher patcher = new PatcherImpl(dtoConversionService, objectMapper,
                emptyFieldDestroyer, requestDtoValidator, patcherHooksCollection);
        patchFlow = new PatchFlowImpl(modelRetriever, patcher, modelSaver,
                dataSecurity, patcherHooksCollection);
    }

    @Test
    void whenPatching_thenPatchedModelShouldBeSaved() throws Exception {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);

        patchFlow.patch(descriptor, patchToChangeNameTo(AFTER_PATCHING));

        assertThatSavedModelWithNewName(AFTER_PATCHING);
    }

    private void whenRetrieveModelThenReturnTestModelWithName(String name) {
        TestModel model = new TestModel();
        model.name = name;

        when(modelRetriever.retrieveModel(descriptor)).thenReturn(model);
    }

    @NotNull
    private JsonPatch patchToChangeNameTo(String newName) throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode(newName));
        return new JsonPatch(Collections.singletonList(operation));
    }

    private void assertThatSavedModelWithNewName(String newName) {
        verify(modelSaver).saveModel(testModelCaptor.capture());
        assertThat(testModelCaptor.getValue(), hasProperty("name", equalTo(newName)));
    }

    @Test
    void whenPatching_thenReturnedModelShouldBeThePatchedOne() throws Exception {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);
        whenSaveModelThenReturnIt();

        Model patchedModel = patchFlow.patch(descriptor, patchToChangeNameTo(AFTER_PATCHING));

        assertThat(patchedModel, instanceOf(TestModel.class));
        assertThat(patchedModel, hasProperty("name", equalTo(AFTER_PATCHING)));
    }

    private void whenSaveModelThenReturnIt() {
        when(modelSaver.saveModel(any())).then(invocation -> invocation.getArgument(0));
    }

    @Test
    void givenDataSecurityDoesNotAllowToPatch_whenPatching_thenAnExceptionShouldBeThrown() {
        whenRetrieveModelThenReturnATestModel();
        doThrow(new ExtremumAccessDeniedException("Access denied"))
                .when(dataSecurity).checkPatchAllowed(any());

        try {
            patchFlow.patch(descriptor, anyPatch());
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    private void whenRetrieveModelThenReturnATestModel() {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);
    }

    @NotNull
    private JsonPatch anyPatch() {
        return new JsonPatch(emptyList());
    }

    @Test
    void givenPatcherHooksExist_whenPatching_thenAllTheHookMethodsShouldBeCalled() {
        whenRetrieveModelThenReturnATestModel();
        whenSaveModelThenReturnIt();

        patchFlow.patch(descriptor, anyPatch());

        verify(patcherHooksCollection).afterPatchAppliedToDto(eq(TestModel.MODEL_NAME), any());
        verify(patcherHooksCollection).beforeSave(eq(TestModel.MODEL_NAME), any());
        verify(patcherHooksCollection).afterSave(eq(TestModel.MODEL_NAME), any());
    }

    @Test
    void whenPatching_thenEmptyFieldDestroyerShouldBeApplied() {
        whenRetrieveModelThenReturnATestModel();

        patchFlow.patch(descriptor, anyPatch());

        verify(emptyFieldDestroyer).destroy(any());
    }
    
    @ModelName(TestModel.MODEL_NAME)
    @ToString
    @Getter
    public static class TestModel extends MongoCommonModel {
        private static final String MODEL_NAME = "TestModel";

        private String name;
    }

    private static class TestModelRequestDto implements RequestDto {
        @JsonProperty
        private String name;
    }

    private static class TestModelDtoConverter
            implements ToRequestDtoConverter<TestModel, TestModelRequestDto>,
            FromRequestDtoConverter<TestModel, TestModelRequestDto> {

        @Override
        public TestModelRequestDto convertToRequest(TestModel model, ConversionConfig config) {
            TestModelRequestDto dto = new TestModelRequestDto();
            dto.name = model.name;
            return dto;
        }

        @Override
        public TestModel convertFromRequest(TestModelRequestDto dto) {
            TestModel model = new TestModel();
            model.name = dto.name;
            return model;
        }

        @Override
        public Class<? extends TestModelRequestDto> getRequestDtoType() {
            return TestModelRequestDto.class;
        }

        @Override
        public String getSupportedModel() {
            return TestModel.MODEL_NAME;
        }
    }

}