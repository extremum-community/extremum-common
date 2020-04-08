package io.extremum.dynamic.controllers;

import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReactiveDynamicModelRestController implements DynamicModelRestController {
    private final JsonBasedDynamicModelService dynamicModelService;

    @PostMapping("/{modelName}")
    public Mono<Response> createDynamicModel(@PathVariable String modelName, @RequestBody Map<String, Object> data) {
        return dynamicModelService.saveModel(new JsonDynamicModel(modelName, data))
                .map(Response::ok)
                .onErrorResume(this::makeErrorResponse);
    }

    private Mono<Response> makeErrorResponse(Throwable throwable) {
        return Mono.fromSupplier(() -> Response.fail(Alert.errorAlert(throwable.getMessage(), null, "xyz-0001"), 400));
    }
}
