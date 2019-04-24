package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionReference;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.AbstractResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a source for a collection that will be exposed
 * via Everything Everything services.
 * The field type must be {@link CollectionReference}.
 * The field must be defined in a subclass of {@link AbstractResponseDto}.
 * Infrastructure will generate the ID and URL automatically.
 *
 * This annotation is used to define 'embedded' collecitons.
 * As collection coordinates, they have <host entity descriptor, host field name>
 * pair. Host entity descriptor is just a {@link Descriptor} of the {@link AbstractResponseDto}
 * to which the annotated collection instance belongs.
 * Host field name is either specified in this annotation or, if omitted, it
 * is considered to be equal to the annotated field name.
 *
 * @author rpuch
 * @see CollectionReference
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MongoEmbeddedCollection {
    String hostFieldName() default "";
}
