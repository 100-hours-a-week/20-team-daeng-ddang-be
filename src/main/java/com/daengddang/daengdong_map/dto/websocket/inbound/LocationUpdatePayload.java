package com.daengddang.daengdong_map.dto.websocket.inbound;

 import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LocationUpdatePayload {

    private final double lat;
    private final double lng;
    private final OffsetDateTime timestamp;

    @JsonCreator
    public LocationUpdatePayload(
            @JsonProperty("lat") double lat,
            @JsonProperty("lng") double lng,
            @JsonProperty("timestamp") OffsetDateTime timestamp
    ) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public static LocationUpdatePayload from(double lat, double lng, OffsetDateTime timestamp) {
        return new LocationUpdatePayload(lat, lng, timestamp);
    }
}
