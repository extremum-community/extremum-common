package io.extremum.everything.services.management;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;

/**
 * @author rpuch
 */
public interface CollectionFetcherManagementService {
    CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id, Projection projection, boolean expand);
}
