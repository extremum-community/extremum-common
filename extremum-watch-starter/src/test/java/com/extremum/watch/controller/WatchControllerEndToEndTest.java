package com.extremum.watch.controller;

import com.extremum.common.utils.DateUtils;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.config.WatchTestConfiguration;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.extremum.watch.Tests.successfulResponse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WatchTestConfiguration.class)
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class WatchControllerEndToEndTest extends TestWithServices {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TextWatchEventRepository eventRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private ZonedDateTime firstEvent;
    private int eventsSize;

    @BeforeAll
    void setUp() {
        TextWatchEvent watchEvent = new TextWatchEvent("\"test1\"", "test", new ModelWithFilledValues());
        watchEvent.setSubscribers(Collections.singleton("Alex"));
        firstEvent = eventRepository
                .save(watchEvent)
                .getCreated();
        eventsSize++;
    }

    @Test
    void testFindAllEvents() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> events = parseEvents(contentAsString);

        assertAll(
                () -> assertEquals(eventsSize, events.size()),
                () -> assertEquals("test1", events.get(0).get("patch"))
        );
    }

    @Test
    void testFindAllEventsAfterFirstEvent() throws Exception {
        TextWatchEvent event = new TextWatchEvent("\"test2\"", "test", new ModelWithFilledValues());
        event.setSubscribers(Collections.singleton("Alex"));
        ZonedDateTime secondEvent = eventRepository.save(event).getCreated();
        eventsSize++;

        ZonedDateTime beforeLastEvent = secondEvent.minusNanos(2);

        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .param("since", DateUtils.formatZonedDateTimeISO_8601(beforeLastEvent))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> events = parseEvents(contentAsString);

        assertAll(
                () -> assertEquals(1, events.size()),
                () -> assertEquals("test2", events.get(0).get("patch"))
        );
    }

    @Test
    void testFindAllEventsAfterTomorrow() throws Exception {
        ZonedDateTime tomorrow = firstEvent.plusDays(1);

        MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                .param("since", DateUtils.formatZonedDateTimeISO_8601(tomorrow))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Map<String, String>> events = parseEvents(contentAsString);

        assertEquals(0, events.size());
    }

    @Test
    void givenCurrentPrincipalIsSubscribedToAModelAndTheModelIsSaved_whenGettingWatchEvents_thenSaveEventShouldBeReturned() {

    }

    private List<Map<String, String>> parseEvents(String response) {
        return JsonPath.parse(response).read("$.result");
    }
}