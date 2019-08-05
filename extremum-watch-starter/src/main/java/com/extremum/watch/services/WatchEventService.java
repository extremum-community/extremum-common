package com.extremum.watch.services;

import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

public interface WatchEventService {
    List<TextWatchEvent> findEvents(String principal, ZonedDateTime since, ZonedDateTime until);
}
