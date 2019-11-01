package io.extremum.common.utils.attribute;

/**
 * @author rpuch
 */
public interface ObjectContentsGraphWalker {
    void walk(Object root, ObjectVisitor visitor);
}
