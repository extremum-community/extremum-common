package io.extremum.mongo.springdata;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple helper to be able to wire the {@link PersistentEntities} from {@link MappingMongoConverter}s beans
 * available in the application context.
 */
class PersistentEntitiesLookup implements FactoryBean<PersistentEntities> {

    private final List<MappingMongoConverter> converters;

    /**
     * Creates a new {@link PersistentEntitiesLookup} for the given {@link MappingMongoConverter}s.
     *
     * @param converters must not be {@literal null}.
     */
    public PersistentEntitiesLookup(List<MappingMongoConverter> converters) {
        this.converters = new ArrayList<>(converters);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public PersistentEntities getObject() {
        List<MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty>> contexts = converters
                .stream()
                .map(MappingMongoConverter::getMappingContext)
                .collect(Collectors.toList());
        return new PersistentEntities(contexts);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return PersistentEntities.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
