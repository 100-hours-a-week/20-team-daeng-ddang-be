package com.daengddang.daengdong_map.dto.response.chat;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HealthcareChatResponse {

    private final String conversationId;
    private final OffsetDateTime answeredAt;
    private final String answer;

    @Builder
    private HealthcareChatResponse(String conversationId,
                                   OffsetDateTime answeredAt,
                                   String answer) {
        this.conversationId = conversationId;
        this.answeredAt = answeredAt;
        this.answer = answer;
    }

    public static HealthcareChatResponse of(String conversationId,
                                            OffsetDateTime answeredAt,
                                            String answer) {
        return HealthcareChatResponse.builder()
                .conversationId(conversationId)
                .answeredAt(answeredAt)
                .answer(answer)
                .build();
    }
}
