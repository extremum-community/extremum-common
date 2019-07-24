package com.extremum.subscription.repository.impl;

import com.extremum.subscription.models.Subscriber;
import com.extremum.subscription.repository.SubscriptionRepository;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    //    TODO add eviction time from properties
    private final RedissonClient client;
    private RMapCache<String, String> subscriptionsMap;

    private static int subscriptionTTL = 30; // in days
    private static int subscriptionIdleTime = 7; // in days
    private static final String SUBSCRIPTIONS = "watch_subscriptions_idx";

    public SubscriptionRepositoryImpl(RedissonClient client, int subscriptionIdleTimeIn, int subscriptionTTLIn) {
        this.client = client;
        subscriptionTTL = subscriptionTTLIn;
        subscriptionIdleTime = subscriptionIdleTimeIn;
    }

    @PostConstruct
    private void init() {
        subscriptionsMap = client.getMapCache(SUBSCRIPTIONS, MapOptions.defaults());
    }

    @Override
    public void save(String modelId, Subscriber subscriber) {
        subscriptionsMap.put(getKeyPattern(subscriber.getId(), modelId), "",
                subscriptionTTL, TimeUnit.DAYS,
                subscriptionIdleTime, TimeUnit.DAYS);
    }

    @Override
    public void remove(String modelId, Subscriber subscriber) {
        subscriptionsMap.remove(getKeyPattern(subscriber.getId(), modelId));
    }

    @Override
    public Collection<String> getAllSubscriptionsIdsBySubscriber(Subscriber subscriber) {
        return subscriptionsMap.keySet(getKeyPattern(subscriber.getId(), "*"))
                .stream()
                .map(key -> key.split(":")[1])
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getAllSubscribersIdsBySubscription(String modelId) {
        return subscriptionsMap.keySet(getKeyPattern("*", modelId))
                .stream()
                .map(key -> key.split(":")[0])
                .collect(Collectors.toList());
    }

    private String getKeyPattern(String subscriberId, String modelId) {
        String delimiter = "%";
        return String.join(delimiter, subscriberId, modelId);
    }
}
