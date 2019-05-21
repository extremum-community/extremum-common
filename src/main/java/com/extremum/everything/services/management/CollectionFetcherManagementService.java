package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.everything.collection.Projection;

import java.util.List;

/**
 * @author rpuch
 */
public interface CollectionFetcherManagementService {
    List<ResponseDto> fetchCollection(CollectionDescriptor id, Projection projection, boolean expand);
}
