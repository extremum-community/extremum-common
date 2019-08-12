package com.extremum.watch.services;

import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;

public interface WatchSubscriptionService {
    void subscribe(Collection<Descriptor> ids, String subscriber);

    void unsubscribe(Collection<Descriptor> ids, String subscriber);

    Collection<String> findAllSubscribersBySubscription(String subscriptionId);
}
