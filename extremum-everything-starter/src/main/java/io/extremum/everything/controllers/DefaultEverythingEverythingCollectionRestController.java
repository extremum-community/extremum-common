package io.extremum.everything.controllers;


import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
    public Flux<ResponseDto> streamCollection(@PathVariable String collectionId, Projection projection,
            @RequestParam(defaultValue = "false") boolean expand) {
        return collectionManagementService.streamCollection(collectionId, projection, expand);
    }
}
