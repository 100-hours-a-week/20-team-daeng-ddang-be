package com.daengddang.daengdong_map.dto.websocket.outbound;

import lombok.Getter;

@Getter
public enum BlockOccupyFailReason {
    INSUFFICIENT_STAY_TIME("블록 체류시간이 부족합니다.");

    private final String message;

    BlockOccupyFailReason(String message) {
        this.message = message;
    }
}
