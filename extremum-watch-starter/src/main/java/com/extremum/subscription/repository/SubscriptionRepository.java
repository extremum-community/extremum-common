package com.extremum.subscription.repository;

import com.extremum.subscription.models.Subscriber;

import java.util.Collection;

public interface SubscriptionRepository {
    void save(String modelId, Subscriber subscriber);

    void remove(String modelId, Subscriber subscriber);

    Collection<String> getAllSubscriptionsIdsBySubscriber(Subscriber subscriber);

    Collection<String> getAllSubscribersIdsBySubscription(String modelId);
}
