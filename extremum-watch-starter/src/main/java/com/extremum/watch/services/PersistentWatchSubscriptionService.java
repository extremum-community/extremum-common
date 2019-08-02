package com.extremum.watch.services;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class PersistentWatchSubscriptionService implements WatchSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void addSubscriptions(Collection<Descriptor> ids, String subscriber) {
        ids.forEach(id -> subscriptionRepository.save(id.getInternalId(), subscriber));
    }

    @Override
    public void deleteSubscription(Descriptor id, String subscriber) {
        subscriptionRepository.remove(id.getInternalId(), subscriber);
    }

    @Override
    public Collection<String> findAllSubscribersBySubscription(String subscriptionId) {
        return subscriptionRepository.getAllSubscribersIdsBySubscription(subscriptionId);
    }
}

