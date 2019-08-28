package io.extremum.everything.services.management;

import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EverythingMultiplexerImpl implements EverythingMultiplexer {
    private final EverythingEverythingManagementService evrEvrManagementService;
    private final EverythingCollectionManagementService collectionManagementService;

    @Override
    public Response get(Descriptor id, Projection projection, boolean expand) {
        if (id.isSingle()) {
            Object result = evrEvrManagementService.get(id, expand);
            return Response.ok(result);
        } else if (id.isCollection()) {
            return fetchCollection(id, projection, expand);
        } else {
            throw new EverythingEverythingException(
                    String.format("'%s' is neither single nor collection", id.getExternalId()));
        }
    }

    private Response fetchCollection(Descriptor id, Projection projection, boolean expand) {
        return collectionManagementService.fetchCollection(id, projection, expand);
    }
}
