package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.services.DefaultDtoConversionService;
import com.extremum.common.exceptions.ConverterNotFoundException;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PatcherServiceTestConfig.class)
class PatcherServiceTest {
    private static final String existingModelId = "id";

    @Autowired
    private DefaultDtoConversionService dtoConversionService;
    @Autowired
    private TestPatcherService patcherService;

    @BeforeEach
    void setUp() {
        dtoConversionService.getConverters().clear();
        dtoConversionService.getConverters().add(new PatchModelConverter());
    }

    @Test
    void patchModel() throws JsonPointerException {
        PatchModel patchedModel = patcherService.patch(existingModelId, createTestPatch());
        assertAll(
                () -> assertNotNull(patchedModel),
                () -> assertEquals("testPatch", patchedModel.getName())
        );
    }

    @Test
    void patchWithoutConverter() {
        dtoConversionService.getConverters().clear();
        assertThrows(ConverterNotFoundException.class, () -> patcherService.patch(existingModelId, createTestPatch()));
    }

    @Test
    void patchWithoutToRequestConverter() {
        dtoConversionService.getConverters().clear();
        dtoConversionService.getConverters().add(new PatchModelDtoConverter());
        assertThrows(ConverterNotFoundException.class, () -> patcherService.patch(existingModelId, createTestPatch()));
    }

    //    This test is check findById that throws exception in test service
    @Test
    void patchMissingModel() {
        assertThrows(ModelNotFoundException.class, () -> patcherService.patch("test", createTestPatch()));
    }

    @Test
    void testFailValidationOnPatch() throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode(""));
        JsonPatch failedValidationPatch = new JsonPatch(Collections.singletonList(operation));
        RequestDtoValidationException exception = assertThrows(RequestDtoValidationException.class, () -> patcherService.patch(existingModelId, failedValidationPatch));
        assertEquals(exception.getConatraintsViolation().size(), 1);
    }

    @Test
    void patchMissingProperty() throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/broken"), new TextNode("1212"));
        JsonPatch brokenPatch = new JsonPatch(Collections.singletonList(operation));
        assertThrows(RuntimeException.class, () -> patcherService.patch(existingModelId, brokenPatch));
    }

    private static JsonPatch createTestPatch() throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode("testPatch"));
        return new JsonPatch(Collections.singletonList(operation));
    }
}
