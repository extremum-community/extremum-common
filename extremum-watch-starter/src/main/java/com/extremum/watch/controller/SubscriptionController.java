package com.extremum.watch.controller;

import com.extremum.common.response.Response;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.exception.WatchException;
import com.extremum.watch.services.SubscriptionService;
import io.extremum.authentication.SecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * This controller responsible for add and delete subscribers for extremum models
 */
//TODO refactor by documentation to /watch
@RestController
@RequestMapping("/api/subscription/{id}")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SecurityProvider securityProvider;
    private final SubscriptionService subscriptionService;

    @PostMapping
    public Response addSubscription(@PathVariable Descriptor id) {
        String principalId = securityProvider.getPrincipal().toString();
        if (principalId == null) {
            throw new WatchException("Cannot find principal on addSubscription()");
        }
        subscriptionService.addSubscription(id, principalId);
        return Response.ok();
    }

    @DeleteMapping
    public Response deleteSubscription(@PathVariable Descriptor id) {
        String principalId = securityProvider.getPrincipal().toString();
        if (principalId == null) {
            throw new WatchException("Cannot find principal on deleteSubscription()");
        }
        subscriptionService.deleteSubscription(id, principalId);
        return Response.ok();
    }
}
