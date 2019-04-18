package com.extremum.common.converters;

import com.extremum.common.utils.DateUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;

public class RedisStringToZonedConverter implements Converter<String, ZonedDateTime> {
    @Override
    public ZonedDateTime convert(String date) {
        return DateUtils.parseZonedDateTimeFromISO_8601(date);
    }
}
