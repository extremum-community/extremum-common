package io.extremum.dynamic.watch;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import reactor.core.publisher.Mono;

public interface DynamicModelWatchService {
    Mono<Void> watchSaveOperation(JsonDynamicModel saved);

    Mono<Void> watchDeleteOperation(JsonDynamicModel model);

    Mono<Void> watchPatchOperation(JsonPatch patch, JsonDynamicModel mdoel);
}
