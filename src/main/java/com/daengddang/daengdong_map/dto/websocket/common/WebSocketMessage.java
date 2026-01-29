package com.daengddang.daengdong_map.dto.websocket.common;

import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class WebSocketMessage<T> {

    private final WebSocketEventType type;
    private final T data;
    private final String message;

    @JsonCreator
    public WebSocketMessage(
            @JsonProperty("type") WebSocketEventType type,
            @JsonProperty("data") T data,
            @JsonProperty("message") String message
    ) {
        this.type = type;
        this.data = data;
        this.message = message;
    }

    public static <T> WebSocketMessage<T> from(WebSocketEventType type, T data) {
        return new WebSocketMessage<>(type, data, type.getMessage());
    }

    public static WebSocketMessage<Void> error(String message) {
        return new WebSocketMessage<>(WebSocketEventType.ERROR, null, message);
    }
}
