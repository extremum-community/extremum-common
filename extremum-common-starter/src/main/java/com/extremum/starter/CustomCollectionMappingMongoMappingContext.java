package com.extremum.starter;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.content.Display;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.util.TypeInformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
class CustomCollectionMappingMongoMappingContext extends MongoMappingContext {
    private FieldNamingStrategy fieldNamingStrategy;

    @Override
    public void setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
        super.setFieldNamingStrategy(fieldNamingStrategy);
        this.fieldNamingStrategy = fieldNamingStrategy;
    }

    @Override
    protected <T> BasicMongoPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        if (typeInformation.getType() == Descriptor.class) {
            return new CustomCollectionMappingMongoPersistentEntity<>(typeInformation);
        }
        
        return super.createPersistentEntity(typeInformation);
    }

    @Override
    public MongoPersistentProperty createPersistentProperty(Property property, BasicMongoPersistentEntity<?> owner,
            SimpleTypeHolder simpleTypeHolder) {
        if (owner.getType() == Descriptor.class) {
            return new CachingMongoPersistentProperty(property, owner, simpleTypeHolder, fieldNamingStrategy) {
                @Override
                public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
                    Field mirrorField = findDescriptorMirrorField(property);
                    return AnnotatedElementUtils.findMergedAnnotation(mirrorField, annotationType);
                }

                @Override
                public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                    return findAnnotation(annotationType) != null;
                }
            };
        }

        return super.createPersistentProperty(property, owner, simpleTypeHolder);
    }

    private Field findDescriptorMirrorField(Property property) {
        try {
            return DescriptorMirror.class.getDeclaredField(property.getName());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CustomCollectionMappingMongoPersistentEntity<T> extends BasicMongoPersistentEntity<T> {

        CustomCollectionMappingMongoPersistentEntity(TypeInformation<T> typeInformation) {
            super(typeInformation);
        }

        @Override
        public String getCollection() {
            if (getType() == Descriptor.class) {
                return Descriptor.COLLECTION;
            }
            return super.getCollection();
        }

        @Override
        public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
            return DescriptorMirror.class.getAnnotation(annotationType);
        }

        @Override
        public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationType) {
            return findAnnotation(annotationType) != null;
        }
    }

    @Document(Descriptor.COLLECTION)
    private static class DescriptorMirror {
        @Id
        private String externalId;
        @Indexed
        private String internalId;
        private String modelType;
        private Descriptor.StorageType storageType;

        @CreatedDate
        private ZonedDateTime created;
        @LastModifiedDate
        private ZonedDateTime modified;
        @Version
        private Long version;

        private boolean deleted;

        private Display display;
    }
}
