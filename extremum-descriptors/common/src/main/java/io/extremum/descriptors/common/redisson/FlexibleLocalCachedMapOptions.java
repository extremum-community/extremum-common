package io.extremum.descriptors.common.redisson;

import lombok.Getter;
import org.redisson.RedissonLocalCachedMap.CacheValue;
import org.redisson.api.LocalCachedMapOptions;

import java.util.function.Predicate;

public class FlexibleLocalCachedMapOptions<K, V> extends LocalCachedMapOptions<K, V> {
    @Getter
    private Predicate<CacheValue> shouldBeCached = (value) -> true;

    protected FlexibleLocalCachedMapOptions(LocalCachedMapOptions<K, V> copy) {
        super(copy);
    }

    public FlexibleLocalCachedMapOptions<K, V> shouldBeCached(Predicate<CacheValue> shouldBeCached) {
        this.shouldBeCached = shouldBeCached;
        return this;
    }

    public static <K, V> FlexibleLocalCachedMapOptions<K, V> defaults() {
        return new FlexibleLocalCachedMapOptions<>(LocalCachedMapOptions.defaults());
    }
}
