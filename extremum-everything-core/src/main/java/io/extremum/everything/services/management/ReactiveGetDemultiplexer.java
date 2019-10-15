package io.extremum.everything.services.management;

import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import reactor.core.publisher.Mono;

public interface ReactiveGetDemultiplexer {
    Mono<Response> get(Descriptor id, Projection projection, boolean expand);
}
