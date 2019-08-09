package com.extremum.watch.end2end;

/**
 * @author rpuch
 */
public interface Probe<T> {
    T sample();

    boolean isFinished(T value);
}
