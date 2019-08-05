package com.extremum.watch.dto.converter;

import com.extremum.common.models.Model;
import com.extremum.watch.dto.PatchedObjectMetadata;
import com.extremum.watch.dto.TextWatchEventResponseDto;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.services.UniversalModelLookup;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class TextWatchEventConverter {
    private final UniversalModelLookup universalModelLookup;

    public TextWatchEventConverter(UniversalModelLookup universalModelLookup) {
        this.universalModelLookup = universalModelLookup;
    }

    public TextWatchEventResponseDto convertToResponseDto(TextWatchEvent event) {
        Model model = universalModelLookup.findModelByInternalId(event.getModelId());
        return new TextWatchEventResponseDto(PatchedObjectMetadata.fromModel(model), event.getUpdateBody());
    }
}
