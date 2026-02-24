package com.daengddang.daengdong_map.dto.response.chat;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HealthcareChatSessionResponse {

    private final String conversationId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime expiresAt;

    @Builder
    private HealthcareChatSessionResponse(String conversationId,
                                          OffsetDateTime createdAt,
                                          OffsetDateTime expiresAt) {
        this.conversationId = conversationId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static HealthcareChatSessionResponse of(String conversationId,
                                                   OffsetDateTime createdAt,
                                                   OffsetDateTime expiresAt) {
        return HealthcareChatSessionResponse.builder()
                .conversationId(conversationId)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }
}
