package com.daengddang.daengdong_map.dto.websocket.common;

import lombok.Getter;

@Getter
public enum WebSocketEventType {

    CONNECTED("연결되었습니다."),
    LOCATION_UPDATE("위치 정보가 수신되었습니다."),
    BLOCK_OCCUPIED("블록 점유에 성공했습니다."),
    BLOCK_OCCUPY_FAILED("블록 점유에 실패했습니다."),
    BLOCK_TAKEN("블록 탈취가 발생했습니다."),
    BLOCKS_SYNC("블록 상태가 동기화되었습니다."),
    WALK_ENDED("산책이 종료되었습니다."),
    ERROR("오류가 발생했습니다.");

    private final String message;

    WebSocketEventType(String message) {
        this.message = message;
    }
}
