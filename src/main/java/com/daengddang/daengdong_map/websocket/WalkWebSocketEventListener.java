package com.daengddang.daengdong_map.websocket;

import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.dto.websocket.outbound.ConnectedPayload;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WalkWebSocketEventListener {

    private static final Pattern WALK_TOPIC_PATTERN = Pattern.compile("^/topic/walks/(\\d+)$");

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher matcher = WALK_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        Long walkId = Long.parseLong(matcher.group(1));
        ConnectedPayload payload = ConnectedPayload.from(walkId, LocalDateTime.now());
        WebSocketMessage<ConnectedPayload> message =
                new WebSocketMessage<>(WebSocketEventType.CONNECTED, payload,
                        WebSocketEventType.CONNECTED.getMessage());
        messagingTemplate.convertAndSend(destination, message);
    }
}
