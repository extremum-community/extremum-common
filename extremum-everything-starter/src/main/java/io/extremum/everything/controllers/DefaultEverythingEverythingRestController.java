package io.extremum.everything.controllers;


import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.response.Response;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static io.extremum.common.response.Response.ok;

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
        return Response.ok(result);
    }

    @DeleteMapping
    public Response remove(@PathVariable Descriptor id) {
        evrEvrManagementService.remove(id);
        return Response.ok();
    }

    @PatchMapping
    public Response patch(@RequestBody JsonPatch patch, @PathVariable Descriptor id,
                          @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.patch(id, patch, expand);
        return Response.ok(result);
    }
}
