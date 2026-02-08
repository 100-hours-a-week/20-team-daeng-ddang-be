package com.daengddang.daengdong_map.util;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class WalkRuntimeStateRegistry {

    private final ConcurrentMap<Long, SyncState> syncStates = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StayState> stayStates = new ConcurrentHashMap<>();

    public SyncState getSyncState(Long walkId) {
        return syncStates.get(walkId);
    }

    public void putSyncState(Long walkId, SyncState state) {
        syncStates.put(walkId, state);
    }

    public StayState getStayState(Long walkId) {
        return stayStates.get(walkId);
    }

    public void putStayState(Long walkId, StayState state) {
        stayStates.put(walkId, state);
    }

    public void clear(Long walkId) {
        if (walkId == null) {
            return;
        }
        syncStates.remove(walkId);
        stayStates.remove(walkId);
    }

    @Getter
    public static class SyncState {
        private final String areaKey;
        private LocalDateTime lastSyncedAt;

        public SyncState(String areaKey, LocalDateTime lastSyncedAt) {
            this.areaKey = areaKey;
            this.lastSyncedAt = lastSyncedAt;
        }

        public void recordLastSyncedAt(LocalDateTime lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
        }
    }

    @Getter
    public static class StayState {
        private final String blockId;
        private final LocalDateTime enteredAt;
        private LocalDateTime lastSeenAt;

        public StayState(String blockId, LocalDateTime enteredAt) {
            this.blockId = blockId;
            this.enteredAt = enteredAt;
            this.lastSeenAt = enteredAt;
        }

        public void recordLastSeenAt(LocalDateTime lastSeenAt) {
            this.lastSeenAt = lastSeenAt;
        }
    }
}
