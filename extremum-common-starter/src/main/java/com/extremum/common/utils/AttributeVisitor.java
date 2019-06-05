package com.extremum.common.utils;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author rpuch
 */
public interface AttributeVisitor {
    void visitAttribute(Attribute attribute);
}
