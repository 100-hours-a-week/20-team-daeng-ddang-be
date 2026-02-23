package com.daengddang.daengdong_map.service.chat.session;

import java.time.OffsetDateTime;
import lombok.Getter;

@Getter
public class ChatSession {

    private final String conversationId;
    private final Long userId;
    private final Long dogId;
    private final OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;

    public ChatSession(String conversationId,
                       Long userId,
                       Long dogId,
                       OffsetDateTime createdAt,
                       OffsetDateTime expiresAt) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.dogId = dogId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void touch(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
