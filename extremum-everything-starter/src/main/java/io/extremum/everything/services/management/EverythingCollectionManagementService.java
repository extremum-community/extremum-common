package io.extremum.everything.services.management;

import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;

public interface EverythingCollectionManagementService {
    Response fetchCollection(String collectionId, Projection projection, boolean expand);
}