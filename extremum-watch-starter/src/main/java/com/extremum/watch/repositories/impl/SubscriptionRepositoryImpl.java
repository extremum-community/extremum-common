package com.extremum.watch.repositories.impl;

import com.extremum.watch.config.SubscriptionProperties;
import com.extremum.watch.models.Subscriber;
import com.extremum.watch.repositories.SubscriptionRepository;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    private final RedissonClient client;
    private final SubscriptionProperties properties;

    private RMapCache<String, String> subscriptionsMap;
    private static final String SUBSCRIPTIONS = "watch_subscriptions_idx";
    private static final String DELIMITER = " / ";

    public SubscriptionRepositoryImpl(RedissonClient client, SubscriptionProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @PostConstruct
    private void init() {
        subscriptionsMap = client.getMapCache(SUBSCRIPTIONS, MapOptions.defaults());
    }

    @Override
    public void save(String modelId, Subscriber subscriber) {
        subscriptionsMap.put(getKeyPattern(subscriber.getId(), modelId), "",
                properties.getTimeToLive(), TimeUnit.DAYS,
                properties.getIdleTime(), TimeUnit.DAYS);
    }

    @Override
    public void remove(String modelId, Subscriber subscriber) {
        subscriptionsMap.remove(getKeyPattern(subscriber.getId(), modelId));
    }

    @Override
    public Collection<String> getAllSubscriptionsIdsBySubscriber(Subscriber subscriber) {
        return subscriptionsMap.keySet(getKeyPattern(subscriber.getId(), "*"))
                .stream()
                .map(key -> key.split(DELIMITER)[1])
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getAllSubscribersIdsBySubscription(String modelId) {
        return subscriptionsMap.keySet(getKeyPattern("*", modelId))
                .stream()
                .map(key -> key.split(DELIMITER)[0])
                .collect(Collectors.toList());
    }

    private String getKeyPattern(String subscriberId, String modelId) {
        return String.join(DELIMITER, subscriberId, modelId);
    }
}
