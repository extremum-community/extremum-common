package com.extremum.watch.services;

import com.extremum.common.mongo.MongoConstants;
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
    public List<TextWatchEvent> findEvents(String subscriber, ZonedDateTime since, ZonedDateTime until) {
        return eventRepository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(subscriber,
                coalesce(since, MongoConstants.DISTANT_PAST), coalesce(until, MongoConstants.DISTANT_FUTURE));
    }

    private ZonedDateTime coalesce(ZonedDateTime first, ZonedDateTime second) {
        if (first != null) {
            return first;
        }
        return second;
    }
}
