package io.extremum.common.collection.conversion;

import io.extremum.common.advice.ResponseDtoHandlingAdvice;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author rpuch
 */
@ControllerAdvice
public class ResponseCollectionsMakeupAdvice extends ResponseDtoHandlingAdvice {
    private final CollectionMakeup makeup;

    public ResponseCollectionsMakeupAdvice(CollectionMakeup makeup) {
        this.makeup = makeup;
    }

    @Override
    protected void applyToResponseDto(ResponseDto responseDto) {
        makeup.applyCollectionMakeup(responseDto);
    }
}
