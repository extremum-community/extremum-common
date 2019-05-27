package com.extremum.everything.controllers;


import com.extremum.common.response.Response;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.services.management.EverythingCollectionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultEverythingEverythingCollectionRestController
        implements EverythingEverythingCollectionRestController {
    private final EverythingCollectionManagementService everythingCollectionManagementService;

    @GetMapping(value = "/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response fetchCollection(@PathVariable String collectionId, Projection projection,
            @RequestParam(defaultValue = "false") boolean expand) {
        return everythingCollectionManagementService.fetchCollection(collectionId, projection, expand);
    }
}
