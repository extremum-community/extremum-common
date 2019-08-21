package io.extremum.jpa.reactive;

import io.extremum.common.models.Model;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.UniversalReactiveModelLoader;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JpaUniversalReactiveModelLoader implements UniversalReactiveModelLoader {
    private final CommonServices commonServices;
    private final Reactifier reactifier;

    @Override
    public Mono<Model> loadByInternalId(String internalId, Class<? extends Model> modelClass) {
        CommonService<? extends Model> commonService = commonServices.findServiceByModel(modelClass);
        return reactifier.mono(() -> commonService.get(internalId));
    }

    @Override
    public Descriptor.StorageType type() {
        return Descriptor.StorageType.POSTGRES;
    }
}
