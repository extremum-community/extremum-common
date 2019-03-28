package com.extremum.everything.controllers;


import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.response.Response;
import com.extremum.everything.services.management.EverythingEverythingManagementService;
import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.extremum.common.response.Response.fail;
import static com.extremum.common.response.Response.ok;

@RequestMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultEverythingEverythingRestController implements EverythingEverythingRestController {
    private EverythingEverythingManagementService evrEvrManagementService;

    public DefaultEverythingEverythingRestController(EverythingEverythingManagementService evrEvrManagementService) {
        this.evrEvrManagementService = evrEvrManagementService;
    }

    @GetMapping
    public Response get(@PathVariable Descriptor id, @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.get(id, expand);
        return ok(result);
    }

    @DeleteMapping
    public Response remove(@PathVariable Descriptor id) {
        // todo exception handler required for handle the EverythingEverythingException
        boolean result = evrEvrManagementService.remove(id);
        return result ? ok() : fail();
    }

    @PatchMapping
    public Response patch(@RequestBody JsonPatch patch, @PathVariable Descriptor id,
                          @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.patch(id, patch, expand);
        return ok(result);
    }
}
