package com.extremum.watch.services;

import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;

public interface WatchSubscriptionService {
    void addSubscriptions(Collection<Descriptor> ids, String subscriber);

    void deleteSubscription(Descriptor id, String subscriber);

    Collection<String> findAllSubscribersBySubscription(String subscriptionId);
}
