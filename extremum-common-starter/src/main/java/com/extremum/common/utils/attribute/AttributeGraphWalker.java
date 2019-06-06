package com.extremum.common.utils.attribute;

/**
 * @author rpuch
 */
public interface AttributeGraphWalker {
    void walk(Object root, AttributeVisitor visitor);
}
