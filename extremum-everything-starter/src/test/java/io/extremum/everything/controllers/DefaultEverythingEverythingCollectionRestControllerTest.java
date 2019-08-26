package io.extremum.everything.controllers;

import io.extremum.everything.services.management.EverythingCollectionManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = EverythingControllersTestConfiguration.class)
class DefaultEverythingEverythingCollectionRestControllerTest {
    private WebTestClient webClient;

    @MockBean
    private EverythingCollectionManagementService collectionManagementService;

    @BeforeEach
    void initClient() {
        Object controller = new DefaultEverythingEverythingCollectionRestController(collectionManagementService);
        webClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void streamsCollection() {
        when(collectionManagementService.streamCollection(eq("dead-beef"), any(), anyBoolean()))
                .thenReturn(Flux.just(new TestResponseDto("first"), new TestResponseDto("second")));

        List<TestResponseDto> dtos = webClient.get().uri("/collection/dead-beef")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(TestResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThatFirstAndSecondAreReturned(dtos);
    }

    private void assertThatFirstAndSecondAreReturned(List<TestResponseDto> dtos) {
        assertThat(dtos, hasSize(2));
        assertThat(dtos.get(0).name, is("first"));
        assertThat(dtos.get(1).name, is("second"));
    }

    @Test
    void whenAnExceptionOccursDuringStreaming_thenItShouldBeHandled() {
        when(collectionManagementService.streamCollection(eq("dead-beef"), any(), anyBoolean()))
                .thenReturn(Flux.error(new RuntimeException("Oops!")));

        String responseText = webClient.get().uri("/collection/dead-beef")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseText, startsWith("event:internal-error\ndata:Internal error "));
    }
}