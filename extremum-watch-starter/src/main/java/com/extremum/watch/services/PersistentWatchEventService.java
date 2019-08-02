package com.extremum.watch.services;

import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersistentWatchEventService implements WatchEventService {
    private final TextWatchEventRepository eventRepository;

    @Override
    public List<TextWatchEvent> findEvents(ZonedDateTime since, ZonedDateTime until) {
        return eventRepository.findByCreatedBetween(since, until);
    }
}
