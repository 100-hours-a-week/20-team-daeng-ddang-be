package com.daengddang.daengdong_map.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketSessionCountListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        String sessionId = SimpMessageHeaderAccessor.wrap(event.getMessage()).getSessionId();
        if (sessionId == null) {
            return;
        }
        if (sessions.add(sessionId)) {
            log.info("[WS] connected sessionId={} active={}", sessionId, sessions.size());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId == null) {
            return;
        }
        if (sessions.remove(sessionId)) {
            log.info("[WS] disconnected sessionId={} active={}", sessionId, sessions.size());
        }
    }
}
