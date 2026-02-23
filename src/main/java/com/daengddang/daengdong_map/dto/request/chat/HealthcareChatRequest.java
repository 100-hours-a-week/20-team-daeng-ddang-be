package com.daengddang.daengdong_map.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthcareChatRequest {

    @NotBlank
    @Size(max = 100)
    private String conversationId;

    @NotBlank
    @Size(max = 200)
    private String message;

    @Size(max = 2048)
    private String imageUrl;

    private HealthcareChatRequest(String conversationId, String message, String imageUrl) {
        this.conversationId = conversationId;
        this.message = message;
        this.imageUrl = imageUrl;
    }

    public static HealthcareChatRequest of(String conversationId, String message, String imageUrl) {
        return new HealthcareChatRequest(conversationId, message, imageUrl);
    }
}
