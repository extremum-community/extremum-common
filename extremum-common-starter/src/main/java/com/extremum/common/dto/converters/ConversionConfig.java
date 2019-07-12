package com.extremum.common.dto.converters;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversionConfig {
    private final boolean expand;

    public static ConversionConfig defaults() {
        return ConversionConfig.builder().build();
    }
}
