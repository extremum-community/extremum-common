package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactivePatchFlow {
    Mono<Model> patch(Descriptor id, JsonPatch patch);
}
