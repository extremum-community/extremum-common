package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;

import java.util.HashSet;
import java.util.Set;

public class WebSocketWatchEventNotificationSenderBase {
    private final Set<String> knownSubscribers = new HashSet<>();

    protected synchronized String getJsonPatch(String subscriber, TextWatchEventNotificationDto notificationDto) {
        if (knownSubscribers.contains(subscriber)) {
            return notificationDto.getJsonPatch();
        } else {
            knownSubscribers.add(subscriber);
            return notificationDto.getFullReplacePatch();
        }
    }
}
