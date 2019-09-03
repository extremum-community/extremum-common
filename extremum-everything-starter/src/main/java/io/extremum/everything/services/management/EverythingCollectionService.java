package io.extremum.everything.services.management;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import reactor.core.publisher.Flux;

/**
 * @author rpuch
 */
public interface EverythingCollectionService {
    CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id, Projection projection, boolean expand);

    Flux<ResponseDto> streamCollection(CollectionDescriptor id, Projection projection, boolean expand);
}
