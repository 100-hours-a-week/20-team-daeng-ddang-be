package com.daengddang.daengdong_map.service.chat.history;

import java.time.Duration;
import java.util.List;

public interface ChatHistoryStore {

    List<ChatHistoryMessage> findAll(String conversationId);

    void append(String conversationId, ChatHistoryMessage message, Duration ttl);

    void remove(String conversationId);
}
