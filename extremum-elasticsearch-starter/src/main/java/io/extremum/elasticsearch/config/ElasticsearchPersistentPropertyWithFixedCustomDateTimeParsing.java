package io.extremum.elasticsearch.config;

import io.extremum.datetime.ApiDateTimeFormat;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentPropertyConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
// TODO: remove when the issue with ZoneDateTime from custom format is solved in spring-data-elasticsearch
class ElasticsearchPersistentPropertyWithFixedCustomDateTimeParsing extends SimpleElasticsearchPersistentProperty {
    @Nullable
    private ElasticsearchPersistentPropertyConverter converterOverride;
    private final ApiDateTimeFormat dateTimeFormat = new ApiDateTimeFormat();

    public ElasticsearchPersistentPropertyWithFixedCustomDateTimeParsing(Property property,
            SimpleElasticsearchPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        super(property, owner, simpleTypeHolder);

        overrideConverterIfNeeded();
    }

    private void overrideConverterIfNeeded() {
        Field field = findAnnotation(Field.class);
        boolean isZonedDateTime = ZonedDateTime.class.isAssignableFrom(getType());

        if (field == null) {
            return;
        }
        if (field.type() != FieldType.Date && field.type() != FieldType.Date_Nanos) {
            return;
        }
        if (!isZonedDateTime) {
            return;
        }

        DateFormat dateFormat = field.format();

        if (dateFormat != DateFormat.custom) {
            return;
        }
        if (!StringUtils.hasLength(field.pattern())) {
            return;
        }

        converterOverride = new ElasticsearchPersistentPropertyConverter() {
            @Override
            public String write(Object property) {
                return dateTimeFormat.format((ZonedDateTime) property);
            }

            @Override
            public Object read(String str) {
                return dateTimeFormat.parse(str);
            }
        };
    }

    @Override
    public ElasticsearchPersistentPropertyConverter getPropertyConverter() {
        if (converterOverride != null) {
            return converterOverride;
        }
        return super.getPropertyConverter();
    }
}
