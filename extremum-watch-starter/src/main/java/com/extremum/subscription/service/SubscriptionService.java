package com.extremum.subscription.service;

import com.extremum.subscription.repository.SubscriptionRepository;
import com.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

//@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository repository;

    public void add(Descriptor id) {
    }

    public void remove(Descriptor id) {
    }
}
