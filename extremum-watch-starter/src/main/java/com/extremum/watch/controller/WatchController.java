package com.extremum.watch.controller;

import com.extremum.common.response.Response;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.dto.TextWatchEventResponseDto;
import com.extremum.watch.dto.converter.TextWatchEventConverter;
import com.extremum.watch.exception.WatchException;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.services.WatchEventService;
import com.extremum.watch.services.WatchSubscriptionService;
import io.extremum.authentication.SecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    private final TextWatchEventConverter textWatchEventConverter;

    /**
     * This method returns all events after date received from request
     */

    // TODO refactor this method to return only events that current security principal can view
    // you can get it from autowired SecurityProvider
    @GetMapping
    public Response getEvents(GetEventsRequest request) {
        List<TextWatchEvent> eventsAfter = watchEventService.findEvents(request.getSince(), request.getUntil());
        return Response.ok(convertToResponseDtos(eventsAfter));
    }

    private List<TextWatchEventResponseDto> convertToResponseDtos(List<TextWatchEvent> eventsAfter) {
        return eventsAfter.stream()
                    .map(textWatchEventConverter::convertToResponseDto)
                    .collect(Collectors.toList());
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

