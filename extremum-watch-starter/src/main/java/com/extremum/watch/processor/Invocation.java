package com.extremum.watch.processor;

/**
 * @author rpuch
 */
public interface Invocation {
    String methodName();

    Object[] args();
}
