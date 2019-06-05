package com.extremum.common.utils;

/**
 * @author rpuch
 */
public interface AttributeGraphWalker {
    void walk(Object root, AttributeVisitor visitor);
}
