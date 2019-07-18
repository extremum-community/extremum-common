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
import com.extremum.everything.security.AllowEverythingForDataAccess;
import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.security.EverythingDataSecurity;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class PatcherImplTest {
    private static final String BEFORE_PATCHING = "Before patching";
    private static final String AFTER_PATCHING = "After patching";

    @InjectMocks
    private PatcherImpl patcher;

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
    private EmptyFieldDestroyer destroyer = new PublicEmptyFieldDestroyer();
    @Mock
    private RequestDtoValidator requestDtoValidator;
    @Spy
    private EverythingDataSecurity dataSecurity = new AllowEverythingForDataAccess();

    @Captor
    private ArgumentCaptor<TestModel> testModelCaptor;

    private final Descriptor descriptor = new Descriptor("external-id");

    @Test
    void whenPatching_thenPatchedModelShouldBeSaved() throws Exception {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);

        patcher.patch(descriptor, patchToChangeNameTo(AFTER_PATCHING));

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
    void whenPatching_thenReturnedModelShouldBeAPatchedOne() throws Exception {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);
        whenSaveModelThenReturnIt();

        Model patchedModel = patcher.patch(descriptor, patchToChangeNameTo(AFTER_PATCHING));

        assertThat(patchedModel, instanceOf(TestModel.class));
        assertThat(patchedModel, hasProperty("name", equalTo(AFTER_PATCHING)));
    }

    private void whenSaveModelThenReturnIt() {
        when(modelSaver.saveModel(any())).then(invocation -> invocation.getArgument(0));
    }

    @Test
    void givenDataSecurityDoesNotAllowToPatch_whenPatching_thenAnExceptionShouldBeThrown() {
        whenRetrieveModelThenReturnTestModelWithName(BEFORE_PATCHING);
        doThrow(new EverythingAccessDeniedException("Access denied"))
                .when(dataSecurity).checkPatchAllowed(any());

        try {
            patcher.patch(descriptor, new JsonPatch(emptyList()));
            fail("An exception should be thrown");
        } catch (EverythingAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }
    
    @ModelName("TestModel")
    @ToString
    @Getter
    public static class TestModel extends MongoCommonModel {
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
            return "TestModel";
        }
    }
}