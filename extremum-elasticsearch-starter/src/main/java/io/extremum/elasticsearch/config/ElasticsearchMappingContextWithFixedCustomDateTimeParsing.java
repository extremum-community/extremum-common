package io.extremum.elasticsearch.config;

import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchPersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * @author rpuch
 */
// TODO: remove when the issue with ZoneDateTime from custom format is solved in spring-data-elasticsearch
public class ElasticsearchMappingContextWithFixedCustomDateTimeParsing extends SimpleElasticsearchMappingContext {
    @Override
    protected ElasticsearchPersistentProperty createPersistentProperty(Property property,
            SimpleElasticsearchPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        return new ElasticsearchPersistentPropertyWithFixedCustomDateTimeParsing(property, owner,
                simpleTypeHolder);
    }
}
