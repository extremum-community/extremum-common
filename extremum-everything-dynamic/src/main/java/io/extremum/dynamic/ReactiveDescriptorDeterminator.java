package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveDescriptorDeterminator {
    Mono<Boolean> isDescriptorForDynamicModel(Descriptor id);
}
