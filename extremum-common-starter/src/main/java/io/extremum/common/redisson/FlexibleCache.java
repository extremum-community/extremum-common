package io.extremum.common.redisson;

import org.redisson.RedissonLocalCachedMap.CacheValue;
import org.redisson.cache.Cache;
import org.redisson.cache.CacheKey;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

class FlexibleCache implements Cache<CacheKey, CacheValue> {
    private final Cache<CacheKey, CacheValue> delegate;
    private final Predicate<CacheValue> shouldBeCached;

    public FlexibleCache(Cache<CacheKey, CacheValue> delegate) {
        this(delegate, value -> true);
    }

    public FlexibleCache(Cache<CacheKey, CacheValue> delegate, Predicate<CacheValue> shouldBeCached) {
        this.delegate = delegate;
        this.shouldBeCached = shouldBeCached;
    }

    @Override
    public CacheValue put(CacheKey key, CacheValue value,
            long ttl, TimeUnit ttlUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
        if (shouldBeCached(value)) {
            return delegate.put(key, value, ttl, ttlUnit, maxIdleTime, maxIdleUnit);
        }
        return null;
    }

    private boolean shouldBeCached(CacheValue value) {
        return shouldBeCached.test(value);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public CacheValue get(Object key) {
        return delegate.get(key);
    }

    @Override
    public CacheValue put(CacheKey key, CacheValue value) {
        if (shouldBeCached(value)) {
            return delegate.put(key, value);
        }
        return null;
    }

    @Override
    public CacheValue remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends CacheKey, ? extends CacheValue> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<CacheKey> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<CacheValue> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<CacheKey, CacheValue>> entrySet() {
        return delegate.entrySet();
    }
}
