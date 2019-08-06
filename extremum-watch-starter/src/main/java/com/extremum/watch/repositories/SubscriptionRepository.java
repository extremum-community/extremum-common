package com.extremum.watch.repositories;

import java.util.Collection;

public interface SubscriptionRepository {
    void saveAll(Collection<String> modelIds, String subscriberId);

    void removeAll(Collection<String> modelIds, String subscriberId);

    Collection<String> getAllSubscriptionsIdsBySubscriber(String subscriberId);

    Collection<String> getAllSubscribersIdsBySubscription(String modelId);
}
