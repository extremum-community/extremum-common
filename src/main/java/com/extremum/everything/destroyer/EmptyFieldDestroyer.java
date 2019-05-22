package com.extremum.everything.destroyer;

/**
 * Destroyer of empty fields in an object.
 * Will be helpful for destroying empty-object fields for clean serialization
 */
public interface EmptyFieldDestroyer {
    <T extends Object> T destroy(T object);
}
