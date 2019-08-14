package io.extremum.everything.controllers;


import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static io.extremum.common.response.Response.ok;

@RestController
@RequiredArgsConstructor
@EverythingExceptionHandlerTarget
public class DefaultEverythingEverythingCollectionRestController
        implements EverythingEverythingCollectionRestController {
    private final EverythingCollectionManagementService everythingCollectionManagementService;

    @GetMapping(value = "/ping")
    public Response ping () {
        return Response.ok();
    }

    @GetMapping(value = "/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response fetchCollection(@PathVariable String collectionId, Projection projection,
            @RequestParam(defaultValue = "false") boolean expand) {
        return everythingCollectionManagementService.fetchCollection(collectionId, projection, expand);
    }
}