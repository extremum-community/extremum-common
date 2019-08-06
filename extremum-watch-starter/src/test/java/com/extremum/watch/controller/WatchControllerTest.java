package com.extremum.watch.controller;

import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.response.Response;
import com.extremum.common.response.ResponseStatusEnum;
import com.extremum.common.utils.DateUtils;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.config.BaseConfig;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.services.WatchEventService;
import com.extremum.watch.services.WatchSubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.jayway.jsonpath.JsonPath;
import io.extremum.authentication.SecurityProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class WatchControllerTest extends TestWithServices {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WatchEventService watchEventService;
    @MockBean
    private SecurityProvider securityProvider;
    @MockBean
    private WatchSubscriptionService watchSubscriptionService;

    @Captor
    private ArgumentCaptor<Collection<Descriptor>> descriptorsCaptor;

    @Test
    void whenPuttingTwoDescriptorsToWatchList_thenBothShouldBeAdded() throws Exception {
        when(securityProvider.getPrincipal()).thenReturn("Alex");

        mockMvc.perform(put("/api/watch")
                .content("[\"dead\",\"beef\"]")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()));

        verify(watchSubscriptionService).addSubscriptions(descriptorsCaptor.capture(), eq("Alex"));
        Collection<Descriptor> savedDescriptors = descriptorsCaptor.getValue();
        //noinspection unchecked
        assertThat(savedDescriptors, containsInAnyOrder(withExternalId("dead"), withExternalId("beef")));
    }

    private Matcher<? super String> successfulResponse() {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                SystemJsonObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));
                Response response = parseResponse(item, mapper);
                return response.getStatus() == ResponseStatusEnum.OK && response.getCode() == 200;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Successful response with status OK and code 200");
            }
        };
    }

    private Response parseResponse(String item, SystemJsonObjectMapper mapper) {
        try {
            return mapper.readValue(new StringReader(item), Response.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Matcher<Descriptor> withExternalId(String externalId) {
        return Matchers.hasProperty("externalId", equalTo(externalId));
    }

    @Test
    void givenOneEventExists_whenGettingTheEventWithoutFiltration_thenItShouldBeReturned() throws Exception {
        when(securityProvider.getPrincipal()).thenReturn("Alex");
        when(watchEventService.findEvents("Alex", Optional.empty(), Optional.empty(), Optional.empty()))
                .thenReturn(singleEventForReplaceFieldToNewValue());

        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Map<String, Object>> events = parseEvents(contentAsString);

        assertThatTheEventIsAsExpected(events);
    }

    @Test
    void givenOneEventExists_whenGettingTheEventWithSinceUntil_thenItShouldBeReturned() throws Exception {
        ZonedDateTime since = ZonedDateTime.now().minusDays(1);
        ZonedDateTime until = since.plusDays(2);

        when(securityProvider.getPrincipal()).thenReturn("Alex");
        when(watchEventService.findEvents(eq("Alex"), any(), any(), eq(Optional.of(10))))
                .thenReturn(singleEventForReplaceFieldToNewValue());

        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .param("since", DateUtils.formatZonedDateTimeISO_8601(since))
                .param("until", DateUtils.formatZonedDateTimeISO_8601(until))
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Map<String, Object>> events = parseEvents(contentAsString);

        assertThatTheEventIsAsExpected(events);
    }

    @NotNull
    private List<TextWatchEvent> singleEventForReplaceFieldToNewValue() throws JsonPointerException, JsonProcessingException {
        JsonPatchOperation operation = new ReplaceOperation(new JsonPointer("/field"), new TextNode("new-value"));
        JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
        String patchAsString = objectMapper.writeValueAsString(jsonPatch);
        return Collections.singletonList(
                new TextWatchEvent(patchAsString, "internalId", new ModelWithFilledValues()));
    }

    private void assertThatTheEventIsAsExpected(List<Map<String, Object>> events) {
        assertThat(events, hasSize(1));
        Map<String, Object> event = events.get(0);

        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) event.get("object");
        assertThat(object, is(notNullValue()));
        assertThat(object.get("id"), is(notNullValue()));
        assertThat(object.get("model"), is("ModelWithExpectedValues"));
        assertThat(object.get("created"), is(notNullValue()));
        assertThat(object.get("modified"), is(notNullValue()));
        assertThat(object.get("version"), is(1));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) event.get("patch");
        assertThat(operations, is(notNullValue()));
        assertThat(operations, hasSize(1));
        Map<String, Object> patchOperation = operations.get(0);
        assertThat(patchOperation.get("path"), is("/field"));
        assertThat(patchOperation.get("op"), is("replace"));
        assertThat(patchOperation.get("value"), is("new-value"));
    }

    private List<Map<String, Object>> parseEvents(String response) {
        return JsonPath.parse(response).read("$.result");
    }

}