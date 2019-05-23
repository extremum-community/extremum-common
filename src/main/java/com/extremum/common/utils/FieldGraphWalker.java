package com.extremum.common.utils;

/**
 * @author rpuch
 */
public interface FieldGraphWalker {
    void walk(Object root, FieldVisitor visitor);
}
