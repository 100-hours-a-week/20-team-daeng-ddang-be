package com.daengddang.daengdong_map.dto.websocket;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WebSocketMessage<T> {

    private final WebSocketEventType type;
    private final T data;
    private final String message;

    @Builder
    private WebSocketMessage(WebSocketEventType type, T data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }

    public static <T> WebSocketMessage<T> of(WebSocketEventType type, T data) {
        return WebSocketMessage.<T>builder()
                .type(type)
                .data(data)
                .build();
    }

    public static WebSocketMessage<Void> error(String message) {
        return WebSocketMessage.<Void>builder()
                .type(WebSocketEventType.ERROR)
                .message(message)
                .build();
    }
}
