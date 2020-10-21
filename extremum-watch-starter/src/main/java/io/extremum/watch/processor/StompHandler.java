package io.extremum.watch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class StompClient {
    @AllArgsConstructor
    @Slf4j
    private static class Message {
        String frame;
        Map<String, String> headers;
        String body;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(frame);
            sb.append('\n');
            for (Map.Entry<String, String> h: headers.entrySet()) {
                sb.append(h.getKey() + ':' + h.getValue());
                sb.append('\n');
            }
            sb.append('\n');
            if (body != null) {
                sb.append(body);
            }
            sb.append('\u0000');

            return sb.toString();
        }

        static Message parse(String raw) {
            String rawWithoutNullChar;
            if (raw.endsWith("\u0000")) {
                rawWithoutNullChar = raw.substring(0, raw.length() - 1);
            } else {
                rawWithoutNullChar = raw;
            }
            String[] lines = rawWithoutNullChar.split("\\n");

            String frame = lines[0];

            Map<String, String> headers = new HashMap<>();
            int i = 1;
            while (i < lines.length && !lines[i].isEmpty()) {
                int colonPos = lines[i].indexOf(':');
                if (colonPos > 0 ) {
                    headers.put(lines[i].substring(0, colonPos), lines[i].substring(colonPos + 1));
                } else {
                    log.debug("Bad STOMP header: '{}'", lines[i]);
                }

                i++;
            }

            String body = null;
            if (i < lines.length) {
                body = Arrays.stream(lines).skip(i).collect(Collectors.joining("\n"));
            }

            return new Message(frame, headers, body);
        }
    }

    @AllArgsConstructor
    private static class Subscription {
        String id;
        String destination;
        WebSocketSession session;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, EmitterProcessor<WebSocketMessage>> emitters = new ConcurrentHashMap<>();

    public Mono<Void> handle(WebSocketSession session) {
        EmitterProcessor<WebSocketMessage> emitter = EmitterProcessor.create();
        emitters.put(session.getId(), emitter);
        return session.send(emitter.publish().autoConnect()).and(
            session.receive().doOnNext(message -> {
                Message stompMessage = Message.parse(message.getPayloadAsText());
                Message response = null;
                Map<String, String> headers = new HashMap<>();

                switch (stompMessage.frame) {
                    case "CONNECT":
                        headers.put("version", "1.1");
                        headers.put("heart-beat", "0,0");
                        response = new Message("CONNECTED", headers, null);
                        break;
                    case "SUBSCRIBE":
                        String id = stompMessage.headers.get("id");
                        String destination = stompMessage.headers.get("destination");
                        subscriptions.put(destination, new Subscription(id, destination, session));
                        break;
                    default:
                        response = new Message("ERROR", headers, null);
                        break;
                }

                if (response != null) {
                    emitter.onNext(session.textMessage(response.toString()));
                }
            })
        );
    }

    public Mono<Void> send(String user, String destination, Object payload) {
        String subscriptionKey = "/user/" + user + destination;
        Subscription subscription = subscriptions.get(subscriptionKey);
        if (subscription != null) {
            EmitterProcessor<WebSocketMessage> emitter = emitters.get(subscription.session.getId());
            if (emitter != null) {
                try {
                    String body = objectMapper.writeValueAsString(payload);
                    Map<String, String> headers = new HashMap<>();

                    headers.put("destination", subscription.destination);
                    headers.put("content-type", "application/json");
                    headers.put("subscription", subscription.id);
                    headers.put("message-id", UUID.randomUUID().toString());
                    headers.put("content-length", String.valueOf(body.getBytes().length));

                    Message message = new Message("MESSAGE", headers, body);

                    emitter.onNext(subscription.session.textMessage(message.toString()));
                } catch (JsonProcessingException e) {
                    log.error("JSON serialization error", e);
                }
            }
        }
        return Mono.empty();
    }
}
