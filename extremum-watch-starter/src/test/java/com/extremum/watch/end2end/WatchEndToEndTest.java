package com.extremum.watch.end2end;

import com.extremum.security.PrincipalSource;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.config.WatchTestConfiguration;
import com.extremum.watch.end2end.fixture.WatchedModel;
import com.extremum.watch.end2end.fixture.WatchedModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.extremum.watch.Tests.successfulResponse;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WatchTestConfiguration.class)
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class WatchEndToEndTest extends TestWithServices {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WatchedModelService watchedModelService;

    private final String principal = UUID.randomUUID().toString();

    @MockBean
    private PrincipalSource principalSource;

    @BeforeEach
    void plugInAFreshPrincipal() {
        when(principalSource.getPrincipal()).thenReturn(Optional.of(principal));
    }

    @Test
    void givenCurrentPrincipalIsSubscribedToAModelAndTheModelIsSaved_whenGettingWatchEvents_thenSaveEventShouldBeReturned()
            throws Exception {
        String externalId = saveModelWithName("old name");
        subscribeTo(externalId);
        patchToChangeName(externalId, "new name");

        List<Map<String, Object>> events = getNonZeroEventsForCurrentPrincipal();

        assertThatOneEventForPatchingNamePropertyIsCreated(externalId, events);
    }

    @SuppressWarnings("SameParameterValue")
    private String saveModelWithName(String name) {
        WatchedModel model = new WatchedModel();
        model.setName(name);
        WatchedModel savedModel = watchedModelService.save(model);
        return savedModel.getUuid().getExternalId();
    }

    private void subscribeTo(String externalId) throws Exception {
        mockMvc.perform(
                put("/api/watch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"" + externalId + "\"]")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();
    }

    @SuppressWarnings("SameParameterValue")
    private void patchToChangeName(String externalId, String newName) throws Exception {
        JsonPatch jsonPatch = new JsonPatch(singletonList(
                new ReplaceOperation(new JsonPointer("/name"), new TextNode(newName))
        ));

        mockMvc.perform(
                patch("/" + externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jsonPatch)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(successfulResponse()))
                .andReturn();
    }

    private List<Map<String, Object>> getNonZeroEventsForCurrentPrincipal() {
        Poller poller = new Poller(Duration.ofSeconds(10));
        return poller.poll(this::getWatchEventsForCurrentPrincipal, events -> events.size() > 0);
    }

    private List<Map<String, Object>> getWatchEventsForCurrentPrincipal() {
        try {
            MvcResult mvcResult = mockMvc.perform(get("/api/watch")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().string(successfulResponse()))
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            return parseEvents(contentAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> parseEvents(String response) {
        return JsonPath.parse(response).read("$.result");
    }

    private void assertThatOneEventForPatchingNamePropertyIsCreated(String externalId,
            List<Map<String, Object>> events) {
        assertThat(events, hasSize(1));

        Map<String, Object> event = events.get(0);

        assertThat(event.get("object"), is(notNullValue()));
        assertThat(event.get("object"), is(instanceOf(Map.class)));
        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) event.get("object");
        assertThat(object, hasEntry(is("id"), equalTo(externalId)));
        assertThat(object, hasEntry(is("model"), is("E2EWatchedModel")));
        assertThat(object, hasKey("created"));
        assertThat(object, hasKey("modified"));
        assertThat(object, hasKey("version"));

        assertThat(event.get("patch"), is(notNullValue()));
        assertThat(event.get("patch"), is(instanceOf(List.class)));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) event.get("patch");
        assertThat(operations, hasSize(1));

        Map<String, Object> operation = operations.get(0);
        assertThat(operation, hasEntry(is("op"), is("replace")));
        assertThat(operation, hasEntry(is("path"), is("/name")));
        assertThat(operation, hasEntry(is("value"), is("new name")));
    }
}