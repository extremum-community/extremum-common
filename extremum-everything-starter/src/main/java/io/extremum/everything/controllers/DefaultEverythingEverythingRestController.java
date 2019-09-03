package io.extremum.everything.controllers;


import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.logging.InternalErrorLogger;
import io.extremum.common.response.Response;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFound;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.EverythingEverythingManagementService;
import io.extremum.everything.services.management.EverythingGetDemultiplexer;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
@ConvertNullDescriptorToModelNotFound
@EverythingExceptionHandlerTarget
@RequiredArgsConstructor
@Slf4j
public class DefaultEverythingEverythingRestController implements EverythingEverythingRestController {
    private final EverythingEverythingManagementService evrEvrManagementService;
    private final EverythingCollectionManagementService collectionManagementService;
    private final EverythingGetDemultiplexer multiplexer;

    private final InternalErrorLogger errorLogger = new InternalErrorLogger(log);

    @GetMapping
    public Response get(@PathVariable Descriptor id, Projection projection,
                        @RequestParam(defaultValue = "false") boolean expand) {
        return multiplexer.get(id, projection, expand);
    }

    @PatchMapping
    public Response patch(@RequestBody JsonPatch patch, @PathVariable Descriptor id,
                          @RequestParam(defaultValue = "false") boolean expand) {
        Object result = evrEvrManagementService.patch(id, patch, expand);
        return Response.ok(result);
    }

    @DeleteMapping
    public Response remove(@PathVariable Descriptor id) {
        evrEvrManagementService.remove(id);
        return Response.ok();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamCollection(@PathVariable String id, Projection projection,
                                                          @RequestParam(defaultValue = "false") boolean expand) {
        return collectionManagementService.streamCollection(id, projection, expand)
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
                .data(errorLogger.logErrorAndReturnId(e))
                .build();
    }
}
