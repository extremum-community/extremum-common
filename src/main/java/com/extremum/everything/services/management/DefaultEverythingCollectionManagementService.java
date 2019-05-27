package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.response.Response;
import com.extremum.everything.collection.Projection;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static com.extremum.common.response.Response.ok;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class DefaultEverythingCollectionManagementService implements EverythingCollectionManagementService {
    private final CollectionDescriptorService collectionDescriptorService;
    private final EverythingEverythingManagementService evrEvrManagementService;

    @Override
    public Response fetchCollection(String collectionId, Projection projection, boolean expand) {
        CollectionDescriptor collectionDescriptor = collectionDescriptorService.retrieveByExternalId(collectionId)
                .orElseThrow(() -> new CollectionDescriptorNotFoundException(
                        String.format("Did not find a collection descriptor by externalId '%s'", collectionId)));

        Collection<ResponseDto> result = evrEvrManagementService.fetchCollection(collectionDescriptor,
                projection, expand);

        return ok(result);
    }
}
