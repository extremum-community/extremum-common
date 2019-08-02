package com.extremum.watch.controller;

import com.extremum.common.response.Response;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.exception.WatchException;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.services.WatchEventService;
import com.extremum.watch.services.WatchSubscriptionService;
import io.extremum.authentication.SecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This controller responsible for find appropriate watch events
 */
@RestController
@RequestMapping("/api/watch")
@RequiredArgsConstructor
public class WatchController {
    private final WatchEventService watchEventService;
    private final SecurityProvider securityProvider;
    private final WatchSubscriptionService watchSubscriptionService;

    /**
     * This method returns all events after date received from request
     */

    // TODO refactor this method to return only events that current security principal can view
    // you can get it from autowired SecurityProvider
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response getAllEventsAfter(@RequestBody(required = false) ZonedDateTime time) {
        List<TextWatchEvent> eventsAfter = watchEventService.findAllEventsAfter(time);
        return Response.ok(eventsAfter);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response addSubscriptions(@RequestBody List<String> idsToWatch) {
        List<Descriptor> descriptors = idsToWatch.stream()
                .map(Descriptor::new)
                .collect(Collectors.toList());

        watchSubscriptionService.addSubscriptions(descriptors, getSubscriber());

        return Response.ok();
    }

    private String getSubscriber() {
        String principalId = securityProvider.getPrincipal().toString();
        if (principalId == null) {
            throw new WatchException("Cannot find principal");
        }
        return principalId;
    }

    @DeleteMapping
    public Response deleteSubscription(@PathVariable Descriptor id) {
        String principalId = getSubscriber();

        watchSubscriptionService.deleteSubscription(id, principalId);
        return Response.ok();
    }

}

