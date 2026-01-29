package com.daengddang.daengdong_map.dto.websocket.common;

import lombok.Getter;

@Getter
public enum WebSocketErrorReason {

    INVALID_WALK_SESSION("유효하지 않은 산책 세션입니다."),
    INVALID_LOCATION("유효하지 않은 위치 정보입니다."),
    INVALID_EVENT_TYPE("유효하지 않은 이벤트 타입입니다.");

    private final String message;

    WebSocketErrorReason(String message) {
        this.message = message;
    }
}
