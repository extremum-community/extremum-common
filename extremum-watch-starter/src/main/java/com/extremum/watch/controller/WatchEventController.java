package com.extremum.watch.controller;

import com.extremum.common.response.Response;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.services.WatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * This controller responsible for find appropriate watch events
 * //TODO Maybe you need to move methods from {@link SubscriptionController} under this controller
 */
@RestController
@RequestMapping("/api/watch")
@RequiredArgsConstructor
public class WatchEventController {
    private final WatchEventService watchEventService;

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
}

