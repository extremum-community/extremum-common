package io.extremum.watch.end2end;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.jayway.jsonpath.JsonPath;
import io.extremum.security.*;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.test.poll.Poller;
import io.extremum.test.core.StringResponseMatchers;
import io.extremum.watch.config.*;
import io.extremum.watch.controller.ReactiveWatchController;
import io.extremum.watch.end2end.fixture.ReactiveWatchedModelService;
import io.extremum.watch.end2end.fixture.WatchedModel;
import io.extremum.watch.processor.StompHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.extremum.test.core.StringResponseMatchers.responseThat;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebFluxTest(value = ReactiveWatchController.class, properties = { "custom.watch.reactive=true", "logging.level.org.springframework=TRACE" })
@ContextConfiguration(classes = {
        ReactiveWatchTestConfiguration.class,
        ReactiveWatchEndToEndTest.class,
        ReactiveWatchConfiguration.class,
        StompHandler.class
})
@TestInstance(Lifecycle.PER_CLASS)
class ReactiveWatchEndToEndTest extends TestWithServices {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReactiveWatchedModelService watchedModelService;

    @MockBean
    private ReactivePrincipalSource principalSource;

    @SpyBean
    private ReactiveRoleSecurity roleSecurity;

    @SpyBean
    private ReactiveDataSecurity dataSecurity;

    private Mono<WatchedModel> modelMono;

    @BeforeEach
    void init() {
        plugInAFreshPrincipal();
        saveAFreshModel();
    }

    private void plugInAFreshPrincipal() {
        String principal = UUID.randomUUID().toString();
        when(principalSource.getPrincipal()).thenReturn(Mono.just(principal));
    }

    private void saveAFreshModel() {
        WatchedModel modelToSave = new WatchedModel();
        modelToSave.setName("old name");
        modelMono = watchedModelService.create(modelToSave);
    }

    @Test
    void givenCurrentPrincipalIsSubscribedToAModelAndTheModelIsPatched_whenGettingWatchEvents_thenOnePatchEventShouldBeReturned()
            throws Exception {
        subscribeToTheModel();
        patchToChangeNameTo("new name");

        getModelExternalId().subscribe(externalId ->
                StepVerifier.create(getNonZeroEventsForCurrentPrincipal())
                        .assertNext(event -> {
                            assertThatEventObjectMetadataIsCorrect(event, externalId);
                            Map<String, Object> operation = getSingleOperation(event);

                            assertThat(operation, hasEntry(is("op"), is("replace")));
                            assertThat(operation, hasEntry(is("path"), is("/name")));
                            assertThat(operation, hasEntry(is("value"), is("new name")));
                        })
                        .verifyComplete()
        );
    }

    private void subscribeToTheModel() {
        String response = getModelExternalId().flatMapMany(externalId ->
                webTestClient.put()
                        .uri("/watch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("[\"" + externalId + "\"]")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().is2xxSuccessful()
                        .returnResult(String.class)
                        .getResponseBody())
                .blockFirst();

        assertThat(response, StringResponseMatchers.successfulResponse());
    }

    private Mono<String> getModelExternalId() {
        return modelMono
                .map(WatchedModel::getUuid)
                .map(Descriptor::getInternalId);
    }

    @SuppressWarnings("SameParameterValue")
    private void patchToChangeNameTo(String newName) throws Exception {
        JsonPatch jsonPatch = new JsonPatch(singletonList(
                new ReplaceOperation(new JsonPointer("/name"), new TextNode(newName))
        ));

        String response = getModelExternalId().flatMapMany(externalId ->
                webTestClient.patch()
                        .uri("/" + externalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(jsonPatch)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().is2xxSuccessful()
                        .returnResult(String.class)
                        .getResponseBody())
                .blockFirst();

        assertThat(response, StringResponseMatchers.successfulResponse());
    }

    private Flux<Map<String, Object>> getNonZeroEventsForCurrentPrincipal() {
        StepVerifier.create(Mono.just(true))
                .thenAwait(Duration.ofMillis(1000))
                .verifyComplete();
        return getWatchEventsForCurrentPrincipal();
    }

    private Flux<Map<String, Object>> getWatchEventsForCurrentPrincipal() {
        return webTestClient.get()
                .uri("/watch")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody()
                .flatMap(body -> Flux.fromIterable(parseEvents(body)));
    }

    private List<Map<String, Object>> parseEvents(String response) {
        return JsonPath.parse(response).read("$");
    }

    private void assertThatEventObjectMetadataIsCorrect(Map<String, Object> event, String externalId) {
        assertThat(event.get("object"), is(notNullValue()));
        assertThat(event.get("object"), is(instanceOf(Map.class)));
        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) event.get("object");
        assertThat(object, hasEntry(is("id"), equalTo(externalId)));
        assertThat(object, hasEntry(is("model"), is("E2EWatchedModel")));
        assertThat(object, hasKey("created"));
        assertThat(object, hasKey("modified"));
        assertThat(object, hasKey("version"));
    }

    private Map<String, Object> getSingleOperation(Map<String, Object> event) {
        assertThat(event.get("patch"), is(notNullValue()));
        assertThat(event.get("patch"), is(instanceOf(List.class)));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) event.get("patch");
        assertThat(operations, hasSize(1));

        return operations.get(0);
    }

    @Test
    void givenCurrentPrincipalIsSubscribedToAModelAndTheModelIsSaved_whenGettingWatchEvents_thenOneSaveEventShouldBeReturned() {
        subscribeToTheModel();
        saveToChangeNameTo("new name");

        getModelExternalId().subscribe(externalId ->
                StepVerifier.create(getNonZeroEventsForCurrentPrincipal())
                        .assertNext(event -> {
                            assertThatEventObjectMetadataIsCorrect(event, externalId);
                            Map<String, Object> operation = getSingleOperation(event);

                            assertThat(operation, hasEntry(is("op"), is("replace")));
                            assertThat(operation, hasEntry(is("path"), is("/")));
                            assertThat(operation, hasEntry(is("value"), is(singletonMap("name", "new name"))));
                        })
                        .verifyComplete()
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void saveToChangeNameTo(String newName) {
        Mono<WatchedModel> savedModelMono = modelMono.flatMap(model -> {
            model.setName(newName);
            return watchedModelService.save(model);
        });
        StepVerifier.create(savedModelMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void givenCurrentPrincipalIsSubscribedToAModelAndTheModelIsDeleted_whenGettingWatchEvents_thenOneDeletionEventShouldBeReturned()
            throws Exception {
        subscribeToTheModel();
        deleteTheModel();

        getModelExternalId().subscribe(externalId ->
                StepVerifier.create(getNonZeroEventsForCurrentPrincipal())
                        .assertNext(event -> {
                            assertThatEventObjectMetadataIsCorrect(event, externalId);
                            Map<String, Object> operation = getSingleOperation(event);

                            assertThat(operation, hasEntry(is("op"), is("remove")));
                            assertThat(operation, hasEntry(is("path"), is("/")));
                            assertThat(operation, not(hasKey("value")));
                        })
                        .verifyComplete()
        );
    }

    private void deleteTheModel() {
        Mono<WatchedModel> deletedModelMono =
                modelMono.flatMap(model -> watchedModelService.delete(model.getId().toString()));
        StepVerifier.create(deletedModelMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleRequiredToWatch_whenSubscribing_thenADeniedExceptionShouldBeThrown()
            throws Exception {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(roleSecurity).checkWatchAllowed(any());

        subscribeToCurrentModelAndGet403();
    }

    private void subscribeToCurrentModelAndGet403() throws Exception {
        String response = webTestClient.put()
                .uri("/watch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\"" + getModelExternalId() + "\"]")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertThat(response, responseThat(hasProperty("code", is(403))));
    }

    @Test
    void givenDataSecurityDoesNotAllowCurrentUserToWatch_whenSubscribing_thenADeniedExceptionShouldBeThrown()
            throws Exception {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(dataSecurity).checkWatchAllowed(any());

        subscribeToCurrentModelAndGet403();
    }
}