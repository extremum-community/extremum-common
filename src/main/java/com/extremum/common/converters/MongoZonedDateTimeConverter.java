package com.extremum.common.converters;

import com.extremum.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
public class MongoZonedDateTimeConverter extends TypeConverter {
    public MongoZonedDateTimeConverter() {
        super(ZonedDateTime.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (!targetClass.isAssignableFrom(ZonedDateTime.class)) {
            throw new IllegalArgumentException("Unexpected class " + targetClass);
        }
        if (!(fromDBObject instanceof String)) {
            throw new IllegalArgumentException("Unexpected type fromDBObject " + fromDBObject);
        }
        String date = (String) fromDBObject;
        return DateUtils.parseZonedDateTimeFromISO_8601(date);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        return Optional.ofNullable(value)
                .filter(ZonedDateTime.class::isInstance)
                .map(ZonedDateTime.class::cast)
                .map(DateUtils::formatZonedDateTimeISO_8601)
                .orElse(null);
    }
}
