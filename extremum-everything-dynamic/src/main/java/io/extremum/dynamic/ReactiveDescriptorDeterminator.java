package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDescriptorDeterminator {
    Mono<String> registerModelName(String modelName);

    Mono<Boolean> isDynamic(Descriptor id);

    Flux<String> getRegisteredModelNames();
}
