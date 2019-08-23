package io.extremum.everything.controllers;


import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@EverythingExceptionHandlerTarget
public class DefaultEverythingEverythingCollectionRestController
        implements EverythingEverythingCollectionRestController {
    private final EverythingCollectionManagementService collectionManagementService;

    @GetMapping(value = "/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response fetchCollection(@PathVariable String collectionId, Projection projection,
            @RequestParam(defaultValue = "false") boolean expand) {
        return collectionManagementService.fetchCollection(collectionId, projection, expand);
    }

    @GetMapping(value = "/collection/{collectionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamCollection(@PathVariable String collectionId, Projection projection,
            @RequestParam(defaultValue = "false") boolean expand) {
        return collectionManagementService.streamCollection(collectionId, projection, expand)
                .map(this::dtoToSse)
                .onErrorResume(e -> Mono.just(throwableToSse(e)));
    }

    private ServerSentEvent<Object> dtoToSse(ResponseDto dto) {
        return ServerSentEvent.builder()
                .data(dto)
                .build();
    }

    private ServerSentEvent<Object> throwableToSse(Throwable e) {
        return ServerSentEvent.builder().event("internal-error")
                .data(e.getMessage())
                .build();
    }
}
