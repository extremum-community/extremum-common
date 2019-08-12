package com.extremum.watch.dto.converter;

import com.extremum.watch.dto.ModelMetadataResponseDto;
import com.extremum.watch.dto.TextWatchEventResponseDto;
import com.extremum.watch.models.ModelMetadata;
import com.extremum.watch.models.TextWatchEvent;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class TextWatchEventConverter {

    public TextWatchEventResponseDto convertToResponseDto(TextWatchEvent event) {
        ModelMetadata metadata = event.getModelMetadata();
        ModelMetadataResponseDto metadataDto = new ModelMetadataResponseDto(
                metadata.getId(),
                metadata.getModel(),
                metadata.getCreated(),
                metadata.getModified(),
                metadata.getVersion()
        );
        return new TextWatchEventResponseDto(metadataDto, event.getJsonPatch());
    }
}
