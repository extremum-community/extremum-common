package io.extremum.common.support;

import io.extremum.common.models.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface UniversalReactiveModelLoader {
    Mono<Model> loadByInternalId(String internalId, Class<? extends Model> modelClass);

    Descriptor.StorageType type();
}
