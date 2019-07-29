package com.extremum.watch.services;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public void addSubscription(Descriptor id, String principalId) {
        subscriptionRepository.save(id.getInternalId(), principalId);
    }

    public void deleteSubscription(Descriptor id, String principalId) {
        subscriptionRepository.remove(id.getInternalId(), principalId);
    }

    public Collection<String> findAllSubscribersBySubscription(String subscriptionId) {
        return subscriptionRepository.getAllSubscribersIdsBySubscription(subscriptionId);
    }
}

