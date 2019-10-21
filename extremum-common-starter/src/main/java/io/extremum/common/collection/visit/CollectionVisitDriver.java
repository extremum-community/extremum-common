package io.extremum.common.collection.visit;

import io.extremum.common.utils.attribute.*;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class CollectionVisitDriver {

    private final CollectionVisitor collectionVisitor;

    private final AttributeGraphWalker deepWalker;
    private final AttributeGraphWalker shallowWalker = new ShallowAttributeGraphWalker();

    public CollectionVisitDriver(VisitDirection visitDirection, CollectionVisitor collectionVisitor) {
        deepWalker = new DeepAttributeGraphWalker(visitDirection, 10, CollectionVisitDriver::shouldGoDeeper);
        this.collectionVisitor = collectionVisitor;
    }

    private static boolean shouldGoDeeper(Object object) {
        return object != null && (!(object instanceof Descriptor));
    }

    public void visitCollectionsInResponseDto(ResponseDto dto) {
        walkResponseDtoWithoutRecursion(dto);
        visitCollectionsInNonResponseDto(dto);
    }

    public void visitCollectionsInNonResponseDto(Object root) {
        AttributeVisitor dtoVisitor = this::walkResponseDtoInAttribute;
        deepWalker.walk(root, new IsResponseDto(dtoVisitor));
    }

    private void walkResponseDtoInAttribute(Attribute dtoAttribute) {
        ResponseDto dto = (ResponseDto) dtoAttribute.value();
        if (dto == null) {
            return;
        }

        walkResponseDtoWithoutRecursion(dto);
    }

    private void walkResponseDtoWithoutRecursion(ResponseDto dto) {
        AttributeVisitor visitor = attribute -> visitCollection(attribute, dto);
        shallowWalker.walk(dto, new IsCollectionReference(visitor));
    }

    private void visitCollection(Attribute attribute, ResponseDto dto) {
        if (attribute.value() == null) {
            return;
        }

        CollectionReference reference = (CollectionReference) attribute.value();

        collectionVisitor.visit(reference, attribute, dto);
    }

    private static class IsResponseDto implements AttributeVisitor {
        private final AttributeVisitor visitor;

        private IsResponseDto(AttributeVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            Object value = attribute.value();
            if (value != null && ResponseDto.class.isAssignableFrom(value.getClass())) {
                visitor.visitAttribute(attribute);
            }
        }
    }

    private static class IsCollectionReference implements AttributeVisitor {
        private final AttributeVisitor visitor;

        private IsCollectionReference(AttributeVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            if (isOfTypeCollectionReference(attribute)) {
                visitor.visitAttribute(attribute);
            }
        }

        private boolean isOfTypeCollectionReference(Attribute attribute) {
            return CollectionReference.class.isAssignableFrom(attribute.type());
        }
    }

}
