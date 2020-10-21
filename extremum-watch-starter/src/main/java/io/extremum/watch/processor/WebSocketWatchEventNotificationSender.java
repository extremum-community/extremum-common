package io.extremum.watch.processor;

import io.extremum.watch.dto.TextWatchEventNotificationDto;
import io.extremum.watch.dto.WebSocketNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
public class WebSocketWatchEventNotificationSender extends WebSocketWatchEventNotificationSenderBase implements WatchEventNotificationSender {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(TextWatchEventNotificationDto notificationDto) {
        for (String subscriber: notificationDto.getSubscribers()) {
            String jsonPatch = getJsonPatch(subscriber, notificationDto);
            messagingTemplate.convertAndSendToUser(subscriber, "/watch", new WebSocketNotificationDto(jsonPatch));
        }
    }
}
