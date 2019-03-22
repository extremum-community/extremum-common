package com.extremum.common.dto.converters.annotations;

import com.extremum.common.dto.converters.DtoConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DtoModelConverter {
    Class<? extends DtoConverter> value();
}
