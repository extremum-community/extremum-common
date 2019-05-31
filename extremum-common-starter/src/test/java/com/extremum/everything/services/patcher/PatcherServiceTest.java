package com.extremum.everything.services.patcher;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PatcherServiceTestConfig.class)
public class PatcherServiceTest {
    @Autowired
    private DtoConversionService dtoConversionService;
    @Autowired
    private TestPatcherService patcherService;

    @Test
    void patchModel() throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode("testPatch"));
        JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
        PatchModel patchedModel = patcherService.patch("test", jsonPatch);

        assertNotNull(patchedModel);
        assertEquals("testPatch", patchedModel.getName());
    }


    @Test
    void patchUnknownProperty() throws JsonPointerException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/test"), new TextNode("1212"));
        JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
        assertThrows(RuntimeException.class, () -> patcherService.patch("id", jsonPatch));
    }
}
