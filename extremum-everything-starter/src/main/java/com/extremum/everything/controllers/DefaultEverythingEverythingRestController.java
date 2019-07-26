package com.extremum.everything.controllers;


import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.response.Response;
import com.extremum.everything.aop.ConvertNullDescriptorToModelNotFound;
import com.extremum.everything.services.management.EverythingEverythingManagementService;
import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.extremum.common.response.Response.ok;

@RestController
@RequestMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
@ConvertNullDescriptorToModelNotFound
@EverythingExceptionHandlerTarget
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
        evrEvrManagementService.remove(id);
        return ok();
    }

    @PatchMapping
    public Response patch(@RequestBody JsonPatch patch, @PathVariable Descriptor id,
                          @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.patch(id, patch, expand);
        return ok(result);
    }
}
