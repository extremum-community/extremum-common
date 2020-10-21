package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import io.extremum.watch.dto.WebSocketNotificationDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReactiveWebSocketWatchEventNotificationSender
        extends WebSocketWatchEventNotificationSenderBase implements WatchEventNotificationSender {
    private final StompHandler stompHandler;

    @Override
    public void send(TextWatchEventNotificationDto notificationDto) {
        for (String subscriber: notificationDto.getSubscribers()) {
            String jsonPatch = getJsonPatch(subscriber, notificationDto);
            stompHandler.send(subscriber, "/watch", new WebSocketNotificationDto(jsonPatch));
        }
    }
}
