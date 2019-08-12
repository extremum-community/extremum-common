package io.extremum.watch.services;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersistentWatchSubscriptionService implements WatchSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void subscribe(Collection<Descriptor> ids, String subscriber) {
        subscriptionRepository.subscribe(descriptorsToInternalIds(ids), subscriber);
    }

    private List<String> descriptorsToInternalIds(Collection<Descriptor> ids) {
        return ids.stream()
                    .map(Descriptor::getInternalId)
                    .collect(Collectors.toList());
    }

    @Override
    public void unsubscribe(Collection<Descriptor> ids, String subscriber) {
        subscriptionRepository.unsubscribe(descriptorsToInternalIds(ids), subscriber);
    }

    @Override
    public Collection<String> findAllSubscribersBySubscription(String subscriptionId) {
        return subscriptionRepository.getAllSubscribersIdsBySubscription(subscriptionId);
    }
}

