package io.extremum.common.descriptor.dao.impl;

import org.redisson.api.map.MapLoader;

public abstract class ModestMapLoader<K, V> implements MapLoader<K, V> {
    @Override
    public final Iterable<K> loadAllKeys() {
        throw new UnsupportedOperationException("We do not allow to load all keys/values");
    }
}
