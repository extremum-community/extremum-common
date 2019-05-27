package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.everything.collection.Projection;

import java.util.Collection;

/**
 * @author rpuch
 */
public interface CollectionFetcherManagementService {
    Collection<ResponseDto> fetchCollection(CollectionDescriptor id, Projection projection, boolean expand);
}
