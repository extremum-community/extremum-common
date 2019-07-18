package com.extremum.common.dto.converters.services;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.StubDtoConverter;
import com.extremum.common.dto.converters.ToResponseDtoConverter;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultDtoConversionServiceTest {
    @InjectMocks
    private DefaultDtoConversionService dtoConversionService;

    @Mock
    private DtoConverters converters;
    @Spy
    private StubDtoConverter stubDtoConverter = new StubDtoConverter();

    @Mock
    private ResponseDto convertedResponse;

    @Test
    void givenToResponseConverterExists_whenConvertUnknownToResponseDto_thenShouldConvertWithTheGivenConverter() {
        when(converters.findToResponseDtoConverter(AModel.class))
                .thenReturn(Optional.of(new AModelToResponseConverter()));

        ResponseDto response = dtoConversionService.convertUnknownToResponseDto(new AModel(),
                ConversionConfig.defaults());
        assertThat(response, is(sameInstance(convertedResponse)));
    }

    @Test
    void givenToResponseConverterDoesNotExist_whenConvertUnknownToResponseDto_thenShouldConvertWithStubConverter() {
        when(converters.findToResponseDtoConverter(AModel.class))
                .thenReturn(Optional.empty());

        ResponseDto response = dtoConversionService.convertUnknownToResponseDto(new AModel(),
                ConversionConfig.defaults());
        assertThatResponseIsStub(response);
    }

    private void assertThatResponseIsStub(ResponseDto response) {
        assertThat(response.getModel(), is("stub"));
    }

    private static class AModel extends MongoCommonModel {
    }

    private class AModelToResponseConverter implements ToResponseDtoConverter<AModel, ResponseDto> {
        @Override
        public ResponseDto convertToResponse(AModel model, ConversionConfig config) {
            return convertedResponse;
        }

        @Override
        public Class<? extends ResponseDto> getResponseDtoType() {
            return null;
        }

        @Override
        public String getSupportedModel() {
            return null;
        }
    }
}