package io.extremum.dynamic;

import io.extremum.dynamic.server.impl.GithubWebhookListenerHttpSchemaServer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubWebhookListenerLauncher implements ApplicationListener<ContextRefreshedEvent> {
    private final GithubWebhookListenerHttpSchemaServer server;
    private volatile boolean alreadyOccurred = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!alreadyOccurred) {
            alreadyOccurred = true;
            server.launch();
        }
    }
}
