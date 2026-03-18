package com.daengddang.daengdong_map.service.chat.history;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatHistoryMessage {

    private String role;
    private String content;

    public static ChatHistoryMessage of(String role, String content) {
        return new ChatHistoryMessage(role, content);
    }
}
