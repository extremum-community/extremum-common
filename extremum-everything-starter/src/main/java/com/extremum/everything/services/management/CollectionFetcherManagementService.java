package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;

/**
 * @author rpuch
 */
public interface CollectionFetcherManagementService {
    CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id, Projection projection, boolean expand);
}
