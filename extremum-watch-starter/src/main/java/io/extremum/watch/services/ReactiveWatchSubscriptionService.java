package io.extremum.watch.services;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ReactiveWatchSubscriptionService {
    Mono<Void> subscribe(Collection<Descriptor> ids, String subscriber);

    Mono<Void> unsubscribe(Collection<Descriptor> ids, String subscriber);

    Collection<String> findAllSubscribersBySubscription(String subscriptionId);
}

