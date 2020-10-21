package io.extremum.watch.config.conditional;

import io.extremum.watch.processor.WatchEventNotificationSender;
import io.extremum.watch.processor.WebSocketWatchEventNotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class BlockingWatchConfiguration {
    @Bean
    WatchEventNotificationSender watchEventNotificationSender(SimpMessagingTemplate messagingTemplate) {
        return new WebSocketWatchEventNotificationSender(messagingTemplate);
    }
}

