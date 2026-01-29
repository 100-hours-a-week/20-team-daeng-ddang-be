package com.daengddang.daengdong_map.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class StayValidator {

    private static final long STAY_SECONDS = 5;

    private final ConcurrentMap<Long, StayState> stayStates = new ConcurrentHashMap<>();

    public boolean isStaySatisfied(Long walkId, String blockId, LocalDateTime timestamp) {
        StayState state = stayStates.get(walkId);
        if (state == null || !state.blockId.equals(blockId)) {
            stayStates.put(walkId, new StayState(blockId, timestamp));
            return false;
        }

        state.lastSeenAt = timestamp;
        Duration stayed = Duration.between(state.enteredAt, timestamp);
        return stayed.getSeconds() >= STAY_SECONDS;
    }

    private static class StayState {
        private final String blockId;
        private final LocalDateTime enteredAt;
        private LocalDateTime lastSeenAt;

        private StayState(String blockId, LocalDateTime enteredAt) {
            this.blockId = blockId;
            this.enteredAt = enteredAt;
            this.lastSeenAt = enteredAt;
        }
    }
}
