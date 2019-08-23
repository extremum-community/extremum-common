package io.extremum.everything.controllers;

import io.extremum.everything.services.management.EverythingCollectionManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {EverythingControllersTestConfiguration.class,
        DefaultEverythingEverythingCollectionRestController.class})
@AutoConfigureMockMvc
class DefaultEverythingEverythingCollectionRestControllerFailingStreamingTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EverythingCollectionManagementService collectionManagementService;

    @Test
    void whenAnExceptionOccursDuringStreaming_thenItShouldBeHandled() throws Exception {
        when(collectionManagementService.streamCollection(anyString(), any(), anyBoolean()))
                .thenReturn(Flux.error(new RuntimeException("Oops!")));

        String responseText = mockMvc.perform(get("/collection/dead-beef").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseText, startsWith("event:internal-error\ndata:Internal error "));
    }
}