package com.extremum.test.poll;

/**
 * @author rpuch
 */
public interface Probe<T> {
    T sample();

    boolean isFinished(T value);
}
