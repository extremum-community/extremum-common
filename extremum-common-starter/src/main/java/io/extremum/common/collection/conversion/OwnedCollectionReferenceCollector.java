package io.extremum.common.collection.conversion;

import io.extremum.common.attribute.Attribute;
import io.extremum.common.collection.visit.CollectionVisitDriver;
import io.extremum.common.walk.VisitDirection;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class OwnedCollectionReferenceCollector implements ReferenceCollector {
    private final ResponseDto rootDto;

    @Override
    public List<ReferenceContext> collectReferences() {
        List<ReferenceContext> collectedReferences = new ArrayList<>();
        CollectionVisitDriver collectionVisitDriver = createDriver(collectedReferences);
        collectionVisitDriver.visitCollectionsInResponseDto(rootDto);
        return collectedReferences;
    }

    private CollectionVisitDriver createDriver(List<ReferenceContext> collectedReferences) {
        return new CollectionVisitDriver(
                VisitDirection.ROOT_TO_LEAVES,
                (reference, attribute, dto) -> collectReferenceIfEligible(reference, attribute,
                        dto, collectedReferences));
    }

    private void collectReferenceIfEligible(CollectionReference<?> reference, Attribute attribute, ResponseDto dto,
                                            List<ReferenceContext> collectedReferences) {
        if (dto.getId() == null) {
            return;
        }
        if (!attribute.isAnnotatedWith(OwnedCollection.class)) {
            return;
        }

        MakeupBrush brush = new OwnedCollectionReferenceBrush(dto, attribute);
        collectedReferences.add(new ReferenceContext(reference, brush));
    }
}
