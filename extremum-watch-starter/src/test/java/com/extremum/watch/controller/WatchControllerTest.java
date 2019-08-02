package com.extremum.watch.controller;

import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.response.Response;
import com.extremum.common.response.ResponseStatusEnum;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.config.BaseConfig;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.WatchEventService;
import com.extremum.watch.services.WatchSubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.extremum.authentication.SecurityProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private TextWatchEventRepository eventRepository;
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

    private ZonedDateTime firstEvent;
    private int eventsSize;

    @BeforeAll
    void setUp() {
        firstEvent = eventRepository
                .save(new TextWatchEvent("test", "test1", "test"))
                .getCreated();
        eventsSize++;
    }

    @Test
    void testFindAllEvents() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<LinkedHashMap<String, String>> events = parseEvents(contentAsString);

        assertAll(
                () -> assertEquals(eventsSize, events.size()),
                () -> assertEquals("test1", events.get(0).get("patch"))
        );
    }

    @Test
    void testFindAllEventsAfterFirstEvent() throws Exception {
        ZonedDateTime secondEvent = eventRepository.save(new TextWatchEvent("test", "test2", "test")).getCreated();
        eventsSize++;

        ZonedDateTime beforeLastEvent = secondEvent.minusNanos(2);

        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beforeLastEvent)))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<LinkedHashMap<String, String>> events = parseEvents(contentAsString);

        assertAll(
                () -> assertEquals(1, events.size()),
                () -> assertEquals("test2", events.get(0).get("patch"))
        );
    }

    @Test
    void testFindAllEventsAfterTomorrow() throws Exception {
        ZonedDateTime newDate = firstEvent.plusDays(1);

        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDate)))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<LinkedHashMap<String, String>> events = parseEvents(contentAsString);

        assertEquals(0, events.size());
    }

    private List<LinkedHashMap<String, String>> parseEvents(String response) {
        return JsonPath.parse(response).read("$.result");
    }

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
}