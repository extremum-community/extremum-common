package com.extremum.common.models.annotation;

import com.extremum.common.dto.ResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelResponseDto {
    Class<? extends ResponseDto> value();
}
