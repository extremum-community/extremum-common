package io.extremum.everything.services.management;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.response.Pagination;
import io.extremum.common.response.Response;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import static io.extremum.common.response.Response.ok;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class DefaultEverythingCollectionManagementService implements EverythingCollectionManagementService {
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;
    private final EverythingCollectionService everythingCollectionService;

    @Override
    public Response fetchCollection(Descriptor descriptor, Projection projection, boolean expand) {
        CollectionDescriptor collectionDescriptor = descriptor.getCollection();
        if (collectionDescriptor == null) {
            throw new IllegalStateException(
                    String.format("For '%s' no collection was in the descriptor", descriptor.getExternalId()));
        }

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

    @Override
    public Flux<ResponseDto> streamCollection(String collectionId, Projection projection, boolean expand) {
        return reactiveCollectionDescriptorService.retrieveByExternalId(collectionId)
                .flatMapMany(collDescriptor
                        -> everythingCollectionService.streamCollection(collDescriptor, projection, expand));
    }
}
