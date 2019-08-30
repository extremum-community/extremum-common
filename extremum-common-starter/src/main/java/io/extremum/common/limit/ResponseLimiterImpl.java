package io.extremum.common.limit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.collection.visit.CollectionVisitDriver;
import io.extremum.common.utils.attribute.Attribute;
import io.extremum.common.utils.attribute.VisitDirection;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;

import java.util.List;

public class ResponseLimiterImpl implements ResponseLimiter {
    private final long topElementsBudgetInBytes;
    private final ObjectMapper objectMapper;

    private final CollectionVisitDriver collectionVisitDriver = new CollectionVisitDriver(
            VisitDirection.LEAVES_TO_ROOT, this::limitCollectionTop);

    public ResponseLimiterImpl(long topElementsBudgetInBytes, ObjectMapper objectMapper) {
        this.topElementsBudgetInBytes = topElementsBudgetInBytes;
        this.objectMapper = objectMapper;
    }

    @Override
    public void limit(ResponseDto responseDto) {
        collectionVisitDriver.visitCollections(responseDto);
    }

    private void limitCollectionTop(CollectionReference reference, Attribute attribute, ResponseDto dto) {
        int topCollectionLimit = capTopLimitBasedOnBudget(reference);
        applyLimitToCollection(reference, topCollectionLimit);
    }

    private int capTopLimitBasedOnBudget(CollectionReference reference) {
        int elementsThatFit = 0;
        long accumulatedElementsSize = 0;

        for (Object element : reference.getTop()) {
            long currentElementJsonSize = estimateJsonSize(element);
            if (accumulatedElementsSize + currentElementJsonSize > topElementsBudgetInBytes) {
                break;
            }

            accumulatedElementsSize += currentElementJsonSize;
            elementsThatFit++;
        }

        if (elementsThatFit == 0 && reference.getTop().size() > 0) {
            elementsThatFit++;
        }

        return elementsThatFit;
    }

    private long estimateJsonSize(Object object) {
        byte[] serializedForm;
        try {
            serializedForm = objectMapper.writer().writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Should not happen as we are serializing in memory", e);
        }
        return serializedForm.length;
    }

    private void applyLimitToCollection(CollectionReference<Object> collectionReference, int topCollectionLimit) {
        List<Object> cappedTop = collectionReference.getTop().subList(0, topCollectionLimit);
        collectionReference.setTop(cappedTop);
    }
}