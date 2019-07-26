package com.extremum.watch.controller;

import com.extremum.watch.config.BaseApplicationTests;
import com.extremum.watch.config.BaseConfig;
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
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(classes = BaseConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class WatchEventControllerTest extends BaseApplicationTests {
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
        firstEvent = eventRepository
                .save(new TextWatchEvent("test", "test1"))
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
        ZonedDateTime secondEvent = eventRepository.save(new TextWatchEvent("test", "test2")).getCreated();
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
}