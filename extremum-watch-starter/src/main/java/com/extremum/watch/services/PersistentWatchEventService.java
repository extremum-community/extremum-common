package com.extremum.watch.services;

import com.extremum.common.mongo.MongoConstants;
import com.extremum.common.spring.data.OffsetBasedPageRequest;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersistentWatchEventService implements WatchEventService {
    private final TextWatchEventRepository eventRepository;

    @Override
    public List<TextWatchEvent> findEvents(String subscriber, Optional<ZonedDateTime> since,
            Optional<ZonedDateTime> until, Optional<Integer> limit) {
        return eventRepository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(subscriber,
                since.orElse(MongoConstants.DISTANT_PAST),
                until.orElse(MongoConstants.DISTANT_FUTURE),
                limit.map(OffsetBasedPageRequest::limit).orElse(Pageable.unpaged())
                );
    }
}
