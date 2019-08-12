package com.extremum.watch.processor;

import com.extremum.watch.dto.TextWatchEventNotificationDto;

/**
 * @author rpuch
 */
public interface WatchEventNotificationSender {
    void send(TextWatchEventNotificationDto notificationDto);
}
