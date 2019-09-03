package io.extremum.everything.services.management;

import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Flux;

public interface EverythingCollectionManagementService {
    Response fetchCollection(Descriptor collectionId, Projection projection, boolean expand);

    Flux<ResponseDto> streamCollection(String collectionId, Projection projection, boolean expand);
}
