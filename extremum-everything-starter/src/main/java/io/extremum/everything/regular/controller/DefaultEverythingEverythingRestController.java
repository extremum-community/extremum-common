package io.extremum.everything.regular.controller;


import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.controllers.CollectionStreamer;
import io.extremum.everything.controllers.EverythingControllers;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import io.extremum.everything.services.management.EverythingGetDemultiplexer;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Api(tags = "Everything Everything")
@Slf4j
@CrossOrigin
@RestController
@EverythingExceptionHandlerTarget
@ConvertNullDescriptorToModelNotFound
@RequestMapping(path = EverythingControllers.EVERYTHING_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultEverythingEverythingRestController implements EverythingEverythingRestController {
    private final EverythingEverythingManagementService evrEvrManagementService;
    private final EverythingGetDemultiplexer demultiplexer;

    private final CollectionStreamer collectionStreamer;

    public DefaultEverythingEverythingRestController(EverythingEverythingManagementService evrEvrManagementService,
                                                     EverythingCollectionManagementService collectionManagementService,
                                                     EverythingGetDemultiplexer demultiplexer) {
        this.evrEvrManagementService = evrEvrManagementService;
        this.demultiplexer = demultiplexer;

        collectionStreamer = new CollectionStreamer(collectionManagementService);
    }

    @ApiOperation(value = "Everything get")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "limit", value = "Limit for a list in a result", example = "5"),
            @ApiImplicitParam(name = "offset", value = "Page of a result list", example = "5"),
            @ApiImplicitParam(name = "since", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2018-09-26T06:47:01.000580-0500"),
            @ApiImplicitParam(name = "until", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2019-09-26T06:47:01.000580-0500"),
    })
    @GetMapping
    public Response get(@PathVariable Descriptor id, Projection projection,
                        @RequestParam(defaultValue = "false") boolean expand) {
        return demultiplexer.get(id, projection, expand);
    }

    @ApiOperation(value = "Everything patch")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "patch", value = "Json-patch query for patching an object by id", paramType = "body",
                    required = true, example = "[{ \"op\": \"replace\", \"path\": \"/baz\", \"value\": \"boo\" },\n" +
                            "{ \"op\": \"add\", \"path\": \"/hello\", \"value\": [\"world\"] },\n" +
                            "{ \"op\": \"remove\", \"path\": \"/foo\" }]")
    })
    @PatchMapping
    public Response patch(@PathVariable Descriptor id, @RequestBody JsonPatch patch,
                          @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.patch(id, patch, expand);
        return Response.ok(result);
    }

    @ApiOperation(value = "Everything remove")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a")
    })
    @DeleteMapping
    public Response remove(@PathVariable Descriptor id) {
        evrEvrManagementService.remove(id);
        return Response.ok();
    }

    @ApiOperation(value = "Stream collections")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID of an object", required = true, example = "ef767667-29f6-457e-b90a-0d14c7fab08a"),
            @ApiImplicitParam(name = "expand", value = "Return expanded object or no", example = "false"),
            @ApiImplicitParam(name = "limit", value = "Limit for a list in a result", example = "5"),
            @ApiImplicitParam(name = "offset", value = "Page of a result list", example = "5"),
            @ApiImplicitParam(name = "since", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2018-09-26T06:47:01.000580-0500"),
            @ApiImplicitParam(name = "until", value = "Date in format uuuu-MM-dd'T'HH:mm:ss.SSSSSSZ", example = "2019-09-26T06:47:01.000580-0500"),
    })
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamCollection(@PathVariable String id, Projection projection,
                                                          @RequestParam(defaultValue = "false") boolean expand) {
        return collectionStreamer.streamCollection(id, projection, expand);
    }
}
