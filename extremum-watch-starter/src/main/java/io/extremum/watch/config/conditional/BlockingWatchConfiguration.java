package io.extremum.watch.config.conditional;

import io.extremum.authentication.api.IdentityFinder;
import io.extremum.authentication.api.SecurityIdentity;
import io.extremum.security.PrincipalSource;
import io.extremum.watch.processor.WatchEventNotificationSender;
import io.extremum.watch.processor.WebSocketWatchEventNotificationSender;
import io.extremum.watch.services.WatchSubscriberIdProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

@Configuration
public class BlockingWatchConfiguration {
    @Bean
    WatchEventNotificationSender watchEventNotificationSender(SimpMessagingTemplate messagingTemplate) {
        return new WebSocketWatchEventNotificationSender(messagingTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    WatchSubscriberIdProvider subscriberIdProvider(PrincipalSource principalSource, IdentityFinder identityFinder) {
        return () -> principalSource.getPrincipal()
                .map(identityFinder::findByPrincipalId)
                .map(SecurityIdentity::getExternalId);
    }
}

