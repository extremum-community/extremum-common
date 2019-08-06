package com.extremum.watch.repositories.impl;

import com.extremum.watch.config.SubscriptionProperties;
import com.extremum.watch.repositories.SubscriptionRepository;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    private final RedissonClient client;
    private final SubscriptionProperties properties;

    private RMapCache<String, String> subscriptionsMap;
    private static final String SUBSCRIPTIONS = "watch_subscriptions_idx";
    private static final String DELIMITER = "/";

    public SubscriptionRepositoryImpl(RedissonClient client, SubscriptionProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @PostConstruct
    private void init() {
        subscriptionsMap = client.getMapCache(SUBSCRIPTIONS, MapOptions.defaults());
    }

    private void save(String modelId, String subscriberId) {
        subscriptionsMap.put(getKeyPattern(subscriberId, modelId), "",
                properties.getTimeToLive(), TimeUnit.DAYS,
                properties.getIdleTime(), TimeUnit.DAYS);
    }

    @Override
    public void subscribe(Collection<String> modelIds, String subscriberId) {
        modelIds.forEach(modelId -> save(modelId, subscriberId));
    }

    private void remove(String modelId, String subscriberId) {
        subscriptionsMap.remove(getKeyPattern(subscriberId, modelId));
    }

    @Override
    public void unsubscribe(Collection<String> modelIds, String subscriberId) {
        modelIds.forEach(modelId -> remove(modelId, subscriberId));
    }

    @Override
    public Collection<String> getAllSubscribersIdsBySubscription(String modelId) {
//        TODO refactor keySet() method to native script for Redis 
        return subscriptionsMap.keySet(getKeyPattern("*", modelId))
                .stream()
                .map(key -> key.split(DELIMITER)[0])
                .collect(Collectors.toList());
    }

    private String getKeyPattern(String subscriberId, String modelId) {
        return String.join(DELIMITER, subscriberId, modelId);
    }
}
