package com.extremum.watch.processor;

import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.support.ModelClasses;
import com.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.extremum.watch.processor.ProcessorTests.assertThatEventMetadataMatchesModelMetadataFully;
import static com.extremum.watch.processor.ProcessorTests.assertThatEventModelIdMatchesModelId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class PatchFlowWatchProcessorTest {
    @InjectMocks
    private PatchFlowWatchProcessor processor;

    @Mock
    private WatchEventConsumer watchEventConsumer;
    @Spy
    private ModelClasses modelClasses = new TestModelClasses();
    @Spy
    private ObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));

    @Captor
    private ArgumentCaptor<TextWatchEvent> watchEventCaptor;

    @Test
    void whenProcessingPatchInvocationInWatchedModel_thenEventShouldBeCreated() throws Exception {
        WatchedModel model = new WatchedModel();
        model.setName("old-name");
        JsonPatch jsonPatch = replaceNameWithNewName();

        processor.process(new TestInvocation("patch", new Object[]{model.getUuid(), jsonPatch}), model);

        verify(watchEventConsumer).consume(watchEventCaptor.capture());
        TextWatchEvent event = watchEventCaptor.getValue();
        assertThatEventModelIdMatchesModelId(model, event);
        assertThatPatchReplacesNameWith(event.getJsonPatch(), "new-name");
        assertThatEventMetadataMatchesModelMetadataFully(model, event);
    }

    @NotNull
    private JsonPatch replaceNameWithNewName() throws JsonPointerException {
        ReplaceOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode("new-name"));
        return new JsonPatch(Collections.singletonList(operation));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertThatPatchReplacesNameWith(String jsonPatchString, String expectedName) throws JSONException {
        JSONArray patch = new JSONArray(jsonPatchString);

        assertThat(patch.length(), is(1));
        JSONObject operation = patch.getJSONObject(0);

        assertThat(operation.get("op"), is("replace"));
        assertThat(operation.get("path"), is("/name"));

        assertThat(operation.getString("value"), is(equalTo(expectedName)));
    }

    @Test
    void whenProcessingPatchInvocationInNonWatchedModel_thenEventShouldBeCreated() throws Exception {
        NonWatchedModel model = new NonWatchedModel();
        JsonPatch jsonPatch = replaceNameWithNewName();

        processor.process(new TestInvocation("patch", new Object[]{model.getUuid(), jsonPatch}), model);

        verify(watchEventConsumer, never()).consume(watchEventCaptor.capture());
    }
}