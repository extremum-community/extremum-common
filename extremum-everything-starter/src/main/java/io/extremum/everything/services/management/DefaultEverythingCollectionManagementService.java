package io.extremum.everything.services.management;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.common.response.Pagination;
import io.extremum.common.response.Response;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import lombok.RequiredArgsConstructor;

import static io.extremum.common.response.Response.ok;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class DefaultEverythingCollectionManagementService implements EverythingCollectionManagementService {
    private final CollectionDescriptorService collectionDescriptorService;
    private final EverythingCollectionService everythingCollectionService;

    @Override
    public Response fetchCollection(String collectionId, Projection projection, boolean expand) {
        CollectionDescriptor collectionDescriptor = collectionDescriptorService.retrieveByExternalId(collectionId)
                .orElseThrow(() -> new CollectionDescriptorNotFoundException(
                        String.format("Did not find a collection descriptor by externalId '%s'", collectionId)));

        CollectionFragment<ResponseDto> fragment = everythingCollectionService.fetchCollection(
                collectionDescriptor, projection, expand);

        Pagination.PaginationBuilder paginationBuilder = Pagination.builder()
                .count(fragment.elements().size());
        projection.getOffset().ifPresent(paginationBuilder::offset);
        fragment.total().ifPresent(paginationBuilder::total);
        projection.getSince().ifPresent(paginationBuilder::since);
        projection.getUntil().ifPresent(paginationBuilder::until);
        Pagination pagination = paginationBuilder.build();

        return ok(fragment.elements(), pagination);
    }
}
