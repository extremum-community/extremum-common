package com.extremum.everything.controllers;


import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.response.Response;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.services.management.EverythingEverythingManagementService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.extremum.common.response.Response.ok;

@RestController
public class DefaultEverythingEverythingCollectionRestController
        implements EverythingEverythingCollectionRestController {
    private EverythingEverythingManagementService evrEvrManagementService;

    public DefaultEverythingEverythingCollectionRestController(
            EverythingEverythingManagementService evrEvrManagementService) {
        this.evrEvrManagementService = evrEvrManagementService;
    }

    @GetMapping(value = "/collection/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response fetchCollection(@PathVariable CollectionDescriptor id, Projection projection,
            @RequestParam(defaultValue = "false") boolean expand) {
        List<ResponseDto> result = evrEvrManagementService.fetchCollection(id, projection, expand);
        return ok(result);
    }
}
