package com.extremum.common.converters;


import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;

import static com.extremum.common.utils.DateUtils.formatZonedDateTimeISO_8601;

public class RedisZonedToStringConverter implements Converter<ZonedDateTime, String>{
    @Override
    public String convert(ZonedDateTime dateTime) {
        return formatZonedDateTimeISO_8601(dateTime);
    }
}
