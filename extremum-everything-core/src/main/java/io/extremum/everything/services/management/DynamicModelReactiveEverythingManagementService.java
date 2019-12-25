package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.services.defaultservices.DefaultDynamicModelReactiveRemover;
import io.extremum.security.ReactiveDataSecurity;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class DynamicModelReactiveEverythingManagementService implements ReactiveEverythingManagementService {
    private final ModelRetriever modelRetriever;
    private final ReactivePatchFlow patchFlow;
    private final ReactiveDataSecurity dataSecurity;
    private final ReactiveDynamicModelGetter reactiveDynamicModelGetter;
    private final DynamicModelDtoConversionService dynamicModelDtoConversionService;
    private final DefaultDynamicModelReactiveRemover dynamicModelRemover;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        log.warn("Dynamic models doesn't supports an 'expand' value with getter; 'expand' value will be omitted");

        return reactiveDynamicModelGetter.get(id.getInternalId())
                .doOnNext(dataSecurity::checkGetAllowed)
                .flatMap(this::convertDynamicModelToResponseDto)
                .switchIfEmpty(Mono.defer(() -> Mono.error(newModelNotFoundException(id))));
    }

    private ModelNotFoundException newModelNotFoundException(Descriptor id) {
        return new ModelNotFoundException(String.format("Nothing was found by '%s'", id.getExternalId()));
    }

    private Mono<ResponseDto> convertDynamicModelToResponseDto(Model model) {
        return dynamicModelDtoConversionService.convertToResponseDtoReactively(model, ConversionConfig.defaults());
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return patchFlow.patch(id, patch).flatMap(this::convertDynamicModelToResponseDto);
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return checkDataSecurityAllowsRemoval(id)
                .then(id.getInternalIdReactively())
                .flatMap(dynamicModelRemover::remove);
    }

    private Mono<Void> checkDataSecurityAllowsRemoval(Descriptor id) {
        return modelRetriever.retrieveModelReactively(id)
                .doOnNext(dataSecurity::checkRemovalAllowed)
                .then();
    }

}
