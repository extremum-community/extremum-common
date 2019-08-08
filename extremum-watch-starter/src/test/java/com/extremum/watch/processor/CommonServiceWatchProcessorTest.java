package com.extremum.watch.processor;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class CommonServiceWatchProcessorTest {
    private static final String WATCHED_MODEL_NAME = "WatchedModel";
    private static final String NON_WATCHED_MODEL_NAME = "NonWatchedModel";

    @InjectMocks
    private CommonServiceWatchProcessor processor;

    @Mock
    private DtoConversionService dtoConversionService;
    @Mock
    private WatchEventConsumer watchEventConsumer;
    @Spy
    private ObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));
    @Mock
    private DescriptorService descriptorService;
    @Mock
    private ModelClasses modelClasses;

    @Captor
    private ArgumentCaptor<TextWatchEvent> watchEventCaptor;

    @Test
    void whenProcessingSaveInvocationOnWatchedModel_thenEventShouldBeCreatedWithReplacingJsonPatch() throws Exception {
        WatchedModel model = new WatchedModel();
        model.setName("the-model");
        when(dtoConversionService.convertUnknownToRequestDto(same(model), any()))
                .thenReturn(new WatchedModelRequestDto(model));

        processor.process(new TestInvocation("save", new Object[]{model}), model);

        verify(watchEventConsumer).consume(watchEventCaptor.capture());
        TextWatchEvent event = watchEventCaptor.getValue();
        assertThatEventModelIdMatchesModelId(model, event);
        assertThatPatchIsForFullReplaceWithName(event.getJsonPatch(), "the-model");
    }

    private void assertThatEventModelIdMatchesModelId(WatchedModel model, TextWatchEvent event) {
        assertThat(event.getModelId(), is(equalTo(model.getId().toString())));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertThatPatchIsForFullReplaceWithName(String jsonPatchString, String expectedName)
            throws JSONException {
        JSONArray patch = new JSONArray(jsonPatchString);

        assertThat(patch.length(), is(1));
        JSONObject operation = patch.getJSONObject(0);

        assertThat(operation.get("op"), is("replace"));
        assertThat(operation.get("path"), is("/"));

        JSONObject value = operation.getJSONObject("value");
        assertThat("Only fields visible externally are expected", keySet(value), is(singleton("name")));
        assertThat(value.get("name"), is(equalTo(expectedName)));
    }

    private Set<Object> keySet(JSONObject object) {
        Set<Object> set = new HashSet<>();
        for (Iterator iterator = object.keys(); iterator.hasNext(); ) {
            set.add(iterator.next());
        }
        return set;
    }

    @Test
    void whenProcessingSaveInvocationOnNonWatchedModel_thenInvocationShouldBeIgnored() throws Exception {
        NonWatchedModel model = new NonWatchedModel();

        processor.process(new TestInvocation("save", new Object[]{model}), model);

        verify(watchEventConsumer, never()).consume(watchEventCaptor.capture());
    }

    @Test
    void whenProcessingDeleteInvocationOnWatchedModel_thenEventShouldBeCreatedWithRemovingJsonPatch() throws Exception {
        WatchedModel model = new WatchedModel();
        String modelInternalId = model.getId().toString();
        when(descriptorService.loadByInternalId(modelInternalId))
                .thenReturn(Optional.ofNullable(model.getUuid()));
        when(modelClasses.getClassByModelName(WATCHED_MODEL_NAME)).thenReturn(modelClass(WatchedModel.class));

        processor.process(new TestInvocation("delete", new Object[]{modelInternalId}), model);

        verify(watchEventConsumer).consume(watchEventCaptor.capture());
        TextWatchEvent event = watchEventCaptor.getValue();
        assertThatEventModelIdMatchesModelId(model, event);
        assertThatPatchIsForFullRemoval(event);
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private Class<Model> modelClass(Class<? extends Model> modelClass) {
        @SuppressWarnings("unchecked")
        Class<Model> recastClass = (Class<Model>) modelClass;
        return recastClass;
    }

    private void assertThatPatchIsForFullRemoval(TextWatchEvent event) throws JSONException {
        JSONArray patch = new JSONArray(event.getJsonPatch());

        assertThat(patch.length(), is(1));
        JSONObject operation = patch.getJSONObject(0);

        assertThat(operation.get("op"), is("remove"));
        assertThat(operation.get("path"), is("/"));

        assertThat(operation.opt("value"), is(nullValue()));
    }

    @Test
    void whenProcessingDeleteInvocationOnNonWatchedModel_thenInvocationShouldBeIgnored() throws Exception {
        NonWatchedModel model = new NonWatchedModel();
        String modelInternalId = model.getId().toString();

        processor.process(new TestInvocation("delete", new Object[]{modelInternalId}), model);

        verify(watchEventConsumer, never()).consume(watchEventCaptor.capture());
    }

    private static class TestInvocation implements Invocation {
        private final String name;
        private final Object[] arguments;

        private TestInvocation(String name, Object[] arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        public String methodName() {
            return name;
        }

        @Override
        public Object[] args() {
            return arguments;
        }
    }

    @Getter
    @Setter
    @CapturedModel
    @ModelName(WATCHED_MODEL_NAME)
    private static class WatchedModel extends MongoCommonModel {
        private String name;

        WatchedModel() {
            setId(new ObjectId());
            setUuid(Descriptor.builder()
                    .externalId("external-id")
                    .internalId(getId().toString())
                    .modelType(WATCHED_MODEL_NAME)
                    .storageType(Descriptor.StorageType.MONGO)
                    .build());
        }
    }

    @Getter
    private static class WatchedModelRequestDto implements RequestDto {
        @JsonProperty
        private final String name;

        WatchedModelRequestDto(WatchedModel model) {
            name = model.getName();
        }
    }

    @Getter
    @Setter
    @ModelName(NON_WATCHED_MODEL_NAME)
    private static class NonWatchedModel extends MongoCommonModel {
        NonWatchedModel() {
            setId(new ObjectId());
            setUuid(Descriptor.builder()
                    .externalId("external-id")
                    .internalId(getId().toString())
                    .modelType(NON_WATCHED_MODEL_NAME)
                    .storageType(Descriptor.StorageType.MONGO)
                    .build());
        }
    }
}