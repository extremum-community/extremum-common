package io.extremum.batch.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.batch.model.BatchRequestDto;
import io.extremum.sharedmodels.constants.HttpStatus;
import io.extremum.sharedmodels.dto.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@AutoConfigureWebTestClient
class BatchControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebTestClient webTestClient;

    private static ClientAndServer mockServer;

    @BeforeAll
    static void setupMockServer() {
        mockServer = ClientAndServer.startClientAndServer(8000);
    }

    @AfterAll
    static void shutdownMockServer() {
        mockServer.stop();
    }

    @Test
    void testSuccessfulPingRequest() throws JsonProcessingException {
        HttpRequest pingRequest = request().withMethod("GET").withPath("/ping");
        HttpResponse okResponse = response().withStatusCode(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(Response.ok()));
        mockServer.when(pingRequest).respond(okResponse);

        BatchRequestDto dto = new BatchRequestDto();
        dto.setId("ping_id");
        dto.setEndpoint("http://localhost:8000/ping");
        dto.setMethod(HttpMethod.GET);

        List<Response> responseBody = webTestClient.post()
                .uri("/v1/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(objectMapper.writeValueAsString(new BatchRequestDto[]{dto})))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(new ParameterizedTypeReference<List<Response>>() {
                }).returnResult().getResponseBody();

        assertThat(responseBody, notNullValue());
        assertThat(responseBody.size(), is(1));
    }
}
