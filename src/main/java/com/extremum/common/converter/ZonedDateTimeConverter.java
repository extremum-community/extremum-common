package com.extremum.common.converter;

import com.extremum.common.utils.DateUtils;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

public class ZonedDateTimeConverter extends TypeConverter implements SimpleValueConverter {
    public ZonedDateTimeConverter() {
        super(ZonedDateTime.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (!targetClass.isAssignableFrom(ZonedDateTime.class)) {
            throw new IllegalArgumentException("Unexpected class " + targetClass);
        }
        if (!(fromDBObject instanceof Date)) {
            throw new IllegalArgumentException("Unexpected fromDBObject " + fromDBObject);
        }
        Date date = (Date) fromDBObject;
        return DateUtils.fromInstant(date.toInstant());
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        return Optional.ofNullable(value)
                .filter(ZonedDateTime.class::isInstance)
                .map(ZonedDateTime.class::cast)
                .map(ZonedDateTime::toInstant)
                .map(Date::from)
                .orElse(null);
    }
}
