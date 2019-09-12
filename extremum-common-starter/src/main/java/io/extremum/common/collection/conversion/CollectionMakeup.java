package io.extremum.common.collection.conversion;

import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface CollectionMakeup {
    void applyCollectionMakeup(ResponseDto rootDto);

    Mono<Void> applyCollectionMakeupReactively(ResponseDto rootDto);
}
