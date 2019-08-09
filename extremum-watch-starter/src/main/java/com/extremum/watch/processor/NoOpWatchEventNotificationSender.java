package com.extremum.watch.processor;

import com.extremum.watch.dto.TextWatchEventNotificationDto;
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
