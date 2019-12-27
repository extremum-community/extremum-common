package io.extremum.dynamic.everything.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Getter
@RequiredArgsConstructor
public class HybridEverythingManagementService implements ReactiveEverythingManagementService {
    private final ReactiveEverythingManagementService defaultModelEverythingManagementService;
    private final ReactiveDynamicModelEverythingManagementService dynamicModelEverythingManagementService;
    private final ReactiveDescriptorDeterminator reactiveDescriptorDeterminator;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        return determineDescriptorAndProcessOperation(id, service -> service.get(id, expand));
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return determineDescriptorAndProcessOperation(id, service -> service.patch(id, patch, expand));
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return determineDescriptorAndProcessOperation(id, service -> service.remove(id));
    }

    private <R> Mono<R> determineDescriptorAndProcessOperation(Descriptor descr, Function<ReactiveEverythingManagementService, Mono<R>> performer) {
        return reactiveDescriptorDeterminator.isDescriptorForDynamicModel(descr)
                .flatMap(isDynamic -> {
                    if (isDynamic) {
                        log.debug("Descriptor {} determined as a descriptor for a dynamic-model and will be processed with {} service",
                                descr, dynamicModelEverythingManagementService.getClass());
                        return performer.apply(dynamicModelEverythingManagementService);
                    } else {
                        log.debug("Descriptor {} determined as a descriptor for a standard-model and will be processed with {} service",
                                descr, defaultModelEverythingManagementService.getClass());
                        return performer.apply(defaultModelEverythingManagementService);
                    }
                });
    }
}
