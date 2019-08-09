package com.extremum.common.utils.poll;

/**
 * @author rpuch
 */
public interface Probe<T> {
    T sample();

    boolean isFinished(T value);
}
