package com.extremum.watch.controller;

import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.response.Response;
import com.extremum.common.response.ResponseStatusEnum;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.config.BaseConfig;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.services.WatchEventService;
import com.extremum.watch.services.WatchSubscriptionService;
import io.extremum.authentication.SecurityProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BaseConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class WatchControllerTest extends TestWithServices {
    @Autowired
    private MockMvc mockMvc;

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
}