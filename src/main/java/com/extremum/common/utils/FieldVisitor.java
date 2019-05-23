package com.extremum.common.utils;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author rpuch
 */
public interface FieldVisitor {
    void visitField(Field field, Supplier<Object> lazyValue);
}
