package io.extremum.watch.controller;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.watch.exception.WatchException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rpuch
 */
class WatchControllersExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                 .standaloneSetup(new TestController())
                 .setControllerAdvice(new WatchControllersExceptionHandler())
                 .build();
    }

    @Test
    void whenExtremumAccessDeniedExceptionIsThrown_thenProper403ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/extremum-access-denied-exception");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(403));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @NotNull
    private JSONObject getSuccessfullyAndParseResponse(String uri) throws Exception {
        MvcResult result = mockMvc.perform(get(uri))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return parseResponse(result);
    }

    @NotNull
    private JSONObject parseResponse(MvcResult result) throws UnsupportedEncodingException, JSONException {
        String content = result.getResponse().getContentAsString();
        return new JSONObject(content);
    }

    @Test
    void whenWatchExceptionIsThrown_thenProper500ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/watch-exception");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(500));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @RestController
    @EverythingExceptionHandlerTarget
    private static class TestController {
        @RequestMapping("/ok")
        Response ok() {
            return Response.ok("Success!");
        }

        @RequestMapping("/watch-exception")
        Response watchException() {
            throw new WatchException("Something is wrong");
        }

        @RequestMapping("/extremum-access-denied-exception")
        Response extremumAccessDeniedException() {
            throw new ExtremumAccessDeniedException("Access denied");
        }
    }
}