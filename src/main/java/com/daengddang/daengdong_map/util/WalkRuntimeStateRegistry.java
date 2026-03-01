package com.daengddang.daengdong_map.util;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalkRuntimeStateRegistry {

    private static final Duration SYNC_TTL = Duration.ofMinutes(30);
    private static final Duration STAY_TTL = Duration.ofMinutes(30);

    private final RedissonClient redissonClient;

    public SyncState getSyncState(Long walkId) {
        RMap<String, String> map = redissonClient.getMap(syncKey(walkId), StringCodec.INSTANCE);
        String areaKey = map.get("areaKey");
        String lastSyncedAt = map.get("lastSyncedAt");
        if (areaKey == null || lastSyncedAt == null) {
            return null;
        }
        return new SyncState(areaKey, LocalDateTime.parse(lastSyncedAt));
    }

    public void putSyncState(Long walkId, SyncState state) {
        RMap<String, String> map = redissonClient.getMap(syncKey(walkId), StringCodec.INSTANCE);
        map.fastPut("areaKey", state.getAreaKey());
        map.fastPut("lastSyncedAt", state.getLastSyncedAt().toString());
        map.expire(SYNC_TTL);
    }

    public StayState getStayState(Long walkId) {
        RMap<String, String> map = redissonClient.getMap(stayKey(walkId), StringCodec.INSTANCE);
        String blockId = map.get("blockId");
        String enteredAt = map.get("enteredAt");
        String lastSeenAt = map.get("lastSeenAt");
        if (blockId == null || enteredAt == null || lastSeenAt == null) {
            return null;
        }
        StayState state = new StayState(blockId, LocalDateTime.parse(enteredAt));
        state.recordLastSeenAt(LocalDateTime.parse(lastSeenAt));
        return state;
    }

    public void putStayState(Long walkId, StayState state) {
        RMap<String, String> map = redissonClient.getMap(stayKey(walkId), StringCodec.INSTANCE);
        map.fastPut("blockId", state.getBlockId());
        map.fastPut("enteredAt", state.getEnteredAt().toString());
        map.fastPut("lastSeenAt", state.getLastSeenAt().toString());
        map.expire(STAY_TTL);
    }

    public void clear(Long walkId) {
        if (walkId == null) {
            return;
        }
        redissonClient.getKeys().delete(syncKey(walkId), stayKey(walkId));
    }

    private String syncKey(Long walkId) {
        return "walk:sync:" + walkId;
    }

    private String stayKey(Long walkId) {
        return "walk:stay:" + walkId;
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
