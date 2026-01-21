package com.daengddang.daengdong_map.dto.websocket;

public enum WebSocketEventType {
    CONNECTED,
    LOCATION_UPDATE,
    BLOCK_OCCUPIED,
    BLOCK_OCCUPY_FAILED,
    BLOCK_TAKEN,
    BLOCKS_SYNC,
    WALK_ENDED,
    ERROR
}
