package com.extremum.common.repository.jpa;

import com.extremum.common.utils.InstanceMethods;

import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author rpuch
 */
class JpaSoftDeletion {
    boolean supportsSoftDeletion(Class<?> domainType) {
        long nonTransientGetDeletedMethodsCount = new InstanceMethods(domainType).stream()
                .filter(this::isPublic)
                .filter(method -> "getDeleted".equals(method.getName()))
                .filter(this::notAnnotatedAsTransient)
                .count();
        return nonTransientGetDeletedMethodsCount > 0;
    }

    private boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    private boolean notAnnotatedAsTransient(Method method) {
        return method.getAnnotation(Transient.class) == null;
    }
}
