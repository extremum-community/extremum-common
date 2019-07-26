package com.extremum.watch.services;

import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchEventService {
    private final TextWatchEventRepository eventRepository;

    public List<TextWatchEvent> findAllEventsAfter(ZonedDateTime time) {
        if (time == null) {
            return eventRepository.findAll();
        } else {
            return eventRepository.findAllByCreatedAfter(time);
        }
    }
}
