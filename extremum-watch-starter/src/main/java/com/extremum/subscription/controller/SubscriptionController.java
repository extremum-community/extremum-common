package com.extremum.subscription.controller;

import com.extremum.common.response.Response;
import com.extremum.subscription.service.SubscriptionService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

//@RestController
@RequestMapping("/subscription/{id}")
@RequiredArgsConstructor
//@ConvertNullDescriptorToModelNotFound
//TODO move into listener's repository, add Secured to get from secured context
public class SubscriptionController {
    private final SubscriptionService service;

    @PostMapping
    Response addSubscription(@PathVariable Descriptor id) {
        service.add(id);
        return null;
    }


    @DeleteMapping
    Response removeSubscription(@PathVariable Descriptor id) {
        service.remove(id);
        return null;
    }
}
