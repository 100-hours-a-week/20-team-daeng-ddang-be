package com.daengddang.daengdong_map.service.chat.session;

import java.time.Duration;
import java.util.Optional;

public interface ChatSessionStore {

    ChatSession create(Long userId, Long dogId, Duration ttl);

    Optional<ChatSession> find(String conversationId);

    void extend(String conversationId, Duration ttl);

    void remove(String conversationId);
}
