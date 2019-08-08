package com.extremum.watch.processor;

import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
    @InjectMocks
    private CommonServiceWatchProcessor processor;

    @Mock
    private DtoConversionService dtoConversionService;
    @Mock
    private WatchEventConsumer watchEventConsumer;
    @Spy
    private final ObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));

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
        assertThat(event.getModelId(), is(equalTo(model.getId().toString())));
        assertThatPatchIsForFullReplaceWithName(event.getJsonPatch(), "the-model");
    }

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
    @ModelName("TestModel")
    private static class WatchedModel extends MongoCommonModel {
        private String name;

        WatchedModel() {
            setId(new ObjectId());
            setUuid(Descriptor.builder()
                    .externalId("external-id")
                    .internalId(getId().toString())
                    .modelType("TestModel")
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
    @ModelName("NonWatchedModel")
    private static class NonWatchedModel extends MongoCommonModel {
        NonWatchedModel() {
            setId(new ObjectId());
            setUuid(Descriptor.builder()
                    .externalId("external-id")
                    .internalId(getId().toString())
                    .modelType("NonWatchedModel")
                    .storageType(Descriptor.StorageType.MONGO)
                    .build());
        }
    }
}