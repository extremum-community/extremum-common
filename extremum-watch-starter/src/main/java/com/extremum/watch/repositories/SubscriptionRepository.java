package com.extremum.watch.repositories;

import java.util.Collection;

public interface SubscriptionRepository {
    void save(String modelId, String subscriberId);

    void remove(String modelId, String subscriberId);

    Collection<String> getAllSubscriptionsIdsBySubscriber(String subscriberId);

    Collection<String> getAllSubscribersIdsBySubscription(String modelId);
}
