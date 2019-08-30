package io.extremum.starter;

import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.annotation.*;
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
 * This extension is solely needed to have effects of @Id, @CreatedDate, @Indexed
 * and so on on Descriptor and CollectionDescriptor classes but have them not-annotated.
 *
 * @author rpuch
 */
class CustomDescriptorCollectionMappingContext extends MongoMappingContext {
    private FieldNamingStrategy fieldNamingStrategy;

    @Override
    public void setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
        super.setFieldNamingStrategy(fieldNamingStrategy);
        this.fieldNamingStrategy = fieldNamingStrategy;
    }

    @Override
    protected <T> BasicMongoPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        if (typeInformation.getType() == Descriptor.class || typeInformation.getType() == CollectionDescriptor.class) {
            return new CustomCollectionMappingMongoPersistentEntity<>(typeInformation);
        }
        
        return super.createPersistentEntity(typeInformation);
    }

    @Override
    public MongoPersistentProperty createPersistentProperty(Property property, BasicMongoPersistentEntity<?> owner,
            SimpleTypeHolder simpleTypeHolder) {
        if (owner.getType() == Descriptor.class) {
            return new MirroringCachingMongoPersistentProperty(property, owner, simpleTypeHolder) {
                @Override
                Property property() {
                    return property;
                }

                @Override
                Class<?> entityMirrorClass() {
                    return DescriptorMirror.class;
                }
            };
        }
        if (owner.getType() == CollectionDescriptor.class) {
            return new MirroringCachingMongoPersistentProperty(property, owner, simpleTypeHolder) {
                @Override
                Property property() {
                    return property;
                }

                @Override
                Class<?> entityMirrorClass() {
                    return CollectionDescriptorMirror.class;
                }
            };
        }

        return super.createPersistentProperty(property, owner, simpleTypeHolder);
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

    private abstract class MirroringCachingMongoPersistentProperty extends CachingMongoPersistentProperty {
        public MirroringCachingMongoPersistentProperty(Property property, BasicMongoPersistentEntity<?> owner,
                                                       SimpleTypeHolder simpleTypeHolder) {
            super(property, owner, simpleTypeHolder, CustomDescriptorCollectionMappingContext.this.fieldNamingStrategy);
        }

        @Override
        public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
            Field mirrorField = findDescriptorMirrorField(property());
            return AnnotatedElementUtils.findMergedAnnotation(mirrorField, annotationType);
        }

        private Field findDescriptorMirrorField(Property property) {
            try {
                return entityMirrorClass().getDeclaredField(property.getName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(
                        String.format("No '%s' field on '%s'", property.getName(), entityMirrorClass()), e);
            }
        }

        abstract Property property();

        abstract Class<?> entityMirrorClass();

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return findAnnotation(annotationType) != null;
        }
    }

    @Document(Descriptor.COLLECTION)
    private static class DescriptorMirror {
        @Id
        private String externalId;

        private Descriptor.Type type;

        @Indexed
        private String internalId;
        private String modelType;
        private Descriptor.StorageType storageType;

        private CollectionDescriptor collection;

        @CreatedDate
        private ZonedDateTime created;
        @LastModifiedDate
        private ZonedDateTime modified;
        @Version
        private Long version;

        private boolean deleted;

        private Display display;

        @Transient
        private boolean single;
    }

    private static class CollectionDescriptorMirror {
        private CollectionDescriptor.Type type;
        private CollectionCoordinates coordinates;
        @Indexed
        private String coordinatesString;
    }
}
