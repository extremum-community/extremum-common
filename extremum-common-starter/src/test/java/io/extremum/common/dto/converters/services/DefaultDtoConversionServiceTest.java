package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.ReactiveToResponseDtoConverter;
import io.extremum.common.dto.converters.StubDtoConverter;
import io.extremum.common.dto.converters.ToResponseDtoConverter;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
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

    @Test
    void givenReactiveToResponseConverterExists_whenConvertUnknownToResponseDtoReactively_thenShouldConvertWithTheGivenConverter() {
        when(converters.findReactiveToResponseDtoConverter(AModel.class))
                .thenReturn(Optional.of(new AReactiveModelToResponseConverter()));

        ResponseDto response = dtoConversionService.convertUnknownToResponseDtoReactively(new AModel(),
                ConversionConfig.defaults()).block();
        assertThat(response, is(sameInstance(convertedResponse)));
    }

    @Test
    void givenReactiveToResponseConverterDoesNotExist_whenConvertUnknownToResponseDtoReactively_thenShouldConvertWithStubConverter() {
        when(converters.findReactiveToResponseDtoConverter(AModel.class))
                .thenReturn(Optional.empty());

        ResponseDto response = dtoConversionService.convertUnknownToResponseDtoReactively(new AModel(),
                ConversionConfig.defaults()).block();
        assertThat(response, is(notNullValue()));
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

    private class AReactiveModelToResponseConverter implements ReactiveToResponseDtoConverter<AModel, ResponseDto> {
        @Override
        public Mono<ResponseDto> convertToResponseReactively(AModel model, ConversionConfig config) {
            return Mono.just(convertedResponse);
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