package com.extremum.everything.services.management;

import com.extremum.common.response.Response;
import com.extremum.everything.collection.Projection;

public interface EverythingCollectionManagementService {
    Response fetchCollection(String collectionId, Projection projection, boolean expand);
}
