package com.extremum.watch.repositories.impl;

import com.extremum.watch.config.WatchProperties;
import com.extremum.watch.repositories.SubscriptionRepository;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisSubscriptionRepository implements SubscriptionRepository {
    private final RedissonClient client;
    private final WatchProperties properties;

    public RedisSubscriptionRepository(RedissonClient client, WatchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void subscribe(Collection<String> modelIds, String subscriberId) {
        modelIds.forEach(modelId -> subscribeToOne(modelId, subscriberId));
    }

    private void subscribeToOne(String modelId, String subscriberId) {
        RSetCache<String> subscribers = subscriptionSet(modelId);
        subscribers.add(subscriberId, properties.getSubscriptionTimeToLiveDays(), TimeUnit.DAYS);
    }

    private RSetCache<String> subscriptionSet(String modelId) {
        return client.getSetCache(subscriptionKey(modelId));
    }

    private String subscriptionKey(String modelId) {
        return "watch-subscription:" + modelId;
    }

    @Override
    public void unsubscribe(Collection<String> modelIds, String subscriberId) {
        modelIds.forEach(modelId -> unsubscribeFromOne(modelId, subscriberId));
    }

    private void unsubscribeFromOne(String modelId, String subscriberId) {
        RSetCache<String> subscribers = subscriptionSet(modelId);
        subscribers.remove(subscriberId);
    }

    @Override
    public Collection<String> getAllSubscribersIdsBySubscription(String modelId) {
        return new ArrayList<>(subscriptionSet(modelId));
    }

}
