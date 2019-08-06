package com.extremum.watch.repositories;

import java.util.Collection;

public interface SubscriptionRepository {
    void subscribe(Collection<String> modelIds, String subscriberId);

    void unsubscribe(Collection<String> modelIds, String subscriberId);

    Collection<String> getAllSubscriptionsIdsBySubscriber(String subscriberId);

    Collection<String> getAllSubscribersIdsBySubscription(String modelId);
}
