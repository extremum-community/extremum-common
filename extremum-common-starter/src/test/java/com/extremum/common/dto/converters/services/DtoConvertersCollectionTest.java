package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.ToResponseDtoConverter;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DtoConvertersCollectionTest {
    private static final String MODEL_NAME = "AModel";

    @Mock
    private FromRequestDtoConverter<Model, RequestDto> fromRequestConverter;
    @Mock
    private ToRequestDtoConverter<Model, RequestDto> toRequestConverter;
    @Mock
    private ToResponseDtoConverter<Model, ResponseDto> toResponseConverter;

    private final DtoConverters emptyConverters = new DtoConvertersCollection(emptyList(), emptyList(), emptyList());

    @Test
    void givenThereIsFromRequestDtoConverter_whenFindingIt_itShouldBeFound() {
        when(fromRequestConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                singletonList(fromRequestConverter), emptyList(), emptyList()
        );

        assertThat(converters.findFromRequestDtoConverter(AModel.class).orElse(null),
                is(sameInstance(fromRequestConverter)));
    }

    @Test
    void givenThereIsNoFromRequestDtoConverter_whenFindingIt_emptyShouldBeReturned() {
        assertThat(emptyConverters.findFromRequestDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @Test
    void givenThereIsToRequestDtoConverter_whenFindingIt_itShouldBeFound() {
        when(toRequestConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                emptyList(), singletonList(toRequestConverter), emptyList()
        );

        assertThat(converters.findToRequestDtoConverter(AModel.class).orElse(null),
                is(sameInstance(toRequestConverter)));
    }

    @Test
    void givenThereIsNoToRequestDtoConverter_whenFindingIt_emptyShouldBeReturned() {
        assertThat(emptyConverters.findToRequestDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @Test
    void givenThereIsToResponseDtoConverter_whenFindingIt_itShouldBeFound() {
        when(toResponseConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                emptyList(), emptyList(), singletonList(toResponseConverter)
        );

        assertThat(converters.findToResponseDtoConverter(AModel.class).orElse(null),
                is(sameInstance(toResponseConverter)));
    }

    @Test
    void givenThereIsNoToResponseDtoConverter_whenFindingIt_emptyShouldBeReturned() {
        assertThat(emptyConverters.findToResponseDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @ModelName(MODEL_NAME)
    private static class AModel extends MongoCommonModel {
    }
}