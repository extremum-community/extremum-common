package com.extremum.common.dto.converters;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.models.Model;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class StubDtoConverter implements ToResponseDtoConverter {
    @Override
    public ResponseDto convertToResponse(Model model, ConversionConfig config) {
        return new StubResponseDto();
    }

    @Override
    public Class<? extends ResponseDto> getResponseDtoType() {
        return ResponseDto.class;
    }

    @Override
    public String getSupportedModel() {
        return "StubModel";
    }

    private static class StubResponseDto implements ResponseDto {
        @Override
        public Descriptor getId() {
            return null;
        }

        @Override
        public Long getVersion() {
            return 0L;
        }

        @Override
        public ZonedDateTime getCreated() {
            return ZonedDateTime.now();
        }

        @Override
        public ZonedDateTime getModified() {
            return null;
        }

        @Override
        public String getModel() {
            return "stub";
        }
    }
}
