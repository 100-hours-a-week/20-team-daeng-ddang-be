package com.daengddang.daengdong_map.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWebSocketBroadcaster {

    private static final String TOPIC_NAME = "ws:broadcast";

    private final RedissonClient redissonClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private final String instanceId = UUID.randomUUID().toString();
    private int listenerId;

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(TOPIC_NAME, StringCodec.INSTANCE);
        listenerId = topic.addListener(String.class, (channel, message) -> handleMessage(message));
    }

    @PreDestroy
    public void unsubscribe() {
        redissonClient.getTopic(TOPIC_NAME, StringCodec.INSTANCE).removeListener(listenerId);
    }

    public void broadcast(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        publish(destination, payload);
    }

    private void publish(String destination, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            RelayEnvelope envelope = new RelayEnvelope(
                    instanceId,
                    destination,
                    payloadJson
            );
            String message = objectMapper.writeValueAsString(envelope);
            redissonClient.getTopic(TOPIC_NAME, StringCodec.INSTANCE).publish(message);
        } catch (JsonProcessingException e) {
            log.warn("Failed to publish websocket event destination={}", destination, e);
        }
    }

    private void handleMessage(String message) {
        try {
            RelayEnvelope envelope = objectMapper.readValue(message, RelayEnvelope.class);
            if (instanceId.equals(envelope.sourceInstanceId())) {
                return;
            }
            Object payload = objectMapper.readValue(envelope.payloadJson(), Object.class);
            messagingTemplate.convertAndSend(envelope.destination(), payload);
        } catch (Exception e) {
            log.warn("Failed to consume websocket relay message", e);
        }
    }

    private record RelayEnvelope(
            String sourceInstanceId,
            String destination,
            String payloadJson
    ) {
    }
}
