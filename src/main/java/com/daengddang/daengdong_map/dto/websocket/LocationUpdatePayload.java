package com.daengddang.daengdong_map.dto.websocket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LocationUpdatePayload {

    private final double lat;
    private final double lng;
    private final LocalDateTime timestamp;

    @Builder
    private LocationUpdatePayload(double lat, double lng, LocalDateTime timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public static LocationUpdatePayload of(double lat, double lng, LocalDateTime timestamp) {
        return LocationUpdatePayload.builder()
                .lat(lat)
                .lng(lng)
                .timestamp(timestamp)
                .build();
    }
}
