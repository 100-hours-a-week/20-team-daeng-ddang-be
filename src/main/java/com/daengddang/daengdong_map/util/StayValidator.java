package com.daengddang.daengdong_map.util;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class StayValidator {

    private static final long STAY_SECONDS = 5;

    private final WalkRuntimeStateRegistry stateRegistry;

    public StayValidator(WalkRuntimeStateRegistry stateRegistry) {
        this.stateRegistry = stateRegistry;
    }

    public boolean isStaySatisfied(Long walkId, String blockId, LocalDateTime timestamp) {
        WalkRuntimeStateRegistry.StayState state = stateRegistry.getStayState(walkId);
        if (state == null || !state.getBlockId().equals(blockId)) {
            stateRegistry.putStayState(walkId, new WalkRuntimeStateRegistry.StayState(blockId, timestamp));
            return false;
        }

        state.recordLastSeenAt(timestamp);
        Duration stayed = Duration.between(state.getEnteredAt(), timestamp);
        return stayed.getSeconds() >= STAY_SECONDS;
    }
}
