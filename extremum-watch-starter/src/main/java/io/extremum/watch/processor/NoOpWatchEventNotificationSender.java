package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class NoOpWatchEventNotificationSender implements WatchEventNotificationSender {
    @Override
    public void send(TextWatchEventNotificationDto notificationDto) {
        // doing nothing
    }
}