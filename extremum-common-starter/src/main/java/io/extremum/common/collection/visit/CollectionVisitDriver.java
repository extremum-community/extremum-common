package io.extremum.common.collection.visit;

import io.extremum.common.attribute.*;
import io.extremum.common.walk.*;
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

    private final ObjectContentsGraphWalker deepWalker;
    private final AttributeGraphWalker shallowWalker = new ShallowAttributeGraphWalker();

    public CollectionVisitDriver(VisitDirection visitDirection, CollectionVisitor collectionVisitor) {
        deepWalker = new DeepContentsGraphWalker(visitDirection, 10, CollectionVisitDriver::shouldGoDeeper);
        this.collectionVisitor = collectionVisitor;
    }

    private static boolean shouldGoDeeper(Object object) {
        return object != null && (!(object instanceof Descriptor));
    }

    public void visitCollectionsInResponseDto(ResponseDto dto) {
        walkResponseDtoWithoutRecursion(dto);

        ObjectVisitor dtoVisitor = this::walkResponseDtoInAttribute;
        deepWalker.walk(dto, new IsResponseDto(dtoVisitor));
    }

    private void walkResponseDtoInAttribute(Object object) {
        ResponseDto dto = (ResponseDto) object;
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

        CollectionReference<?> reference = (CollectionReference<?>) attribute.value();

        collectionVisitor.visit(reference, attribute, dto);
    }

    private static class IsResponseDto implements ObjectVisitor {
        private final ObjectVisitor visitor;

        private IsResponseDto(ObjectVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visit(Object object) {
            if (ResponseDto.class.isAssignableFrom(object.getClass())) {
                visitor.visit(object);
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
