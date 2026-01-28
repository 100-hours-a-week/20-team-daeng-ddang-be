package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.BlockIdUtil;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockSyncEntry;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlocksSyncPayload;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class BlockSyncService {

    private static final int AREA_SIZE = 13;
    private static final long SYNC_MIN_INTERVAL_SECONDS = 2;

    private final BlockOwnershipRepository blockOwnershipRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<Long, SyncState> syncStates = new ConcurrentHashMap<>();

    public String toAreaKey(int blockX, int blockY) {
        int areaX = Math.floorDiv(blockX, AREA_SIZE);
        int areaY = Math.floorDiv(blockY, AREA_SIZE);
        return areaX + "_" + areaY;
    }

    public void syncBlocks(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime now) {
        SyncState state = syncStates.get(walkId);
        if (state == null || !state.areaKey.equals(areaKey)) {
            syncStates.put(walkId, new SyncState(areaKey, now));
            sendBlocksSync(blockX, blockY, areaKey);
            return;
        }

        Duration since = Duration.between(state.lastSyncedAt, now);
        if (since.getSeconds() < SYNC_MIN_INTERVAL_SECONDS) {
            return;
        }

        state.lastSyncedAt = now;
        sendBlocksSync(blockX, blockY, areaKey);
    }

    public void syncBlocksOnAreaChange(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime now) {
        SyncState state = syncStates.get(walkId);
        if (state == null || !state.areaKey.equals(areaKey)) {
            syncStates.put(walkId, new SyncState(areaKey, now));
            sendBlocksSync(blockX, blockY, areaKey);
        }
    }

    private void sendBlocksSync(int blockX, int blockY, String areaKey) {
        AreaRange range = toAreaRange(blockX, blockY);
        List<BlockSyncEntry> entries = blockOwnershipRepository.findAllByBlockRange(
                        range.minX, range.maxX, range.minY, range.maxY
                ).stream()
                .map(ownership -> BlockSyncEntry.from(
                        BlockIdUtil.toBlockId(ownership.getBlock().getX(), ownership.getBlock().getY()),
                        ownership.getDog().getId()
                ))
                .toList();

        BlocksSyncPayload payload = BlocksSyncPayload.from(entries);
        WebSocketMessage<BlocksSyncPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCKS_SYNC, payload,
                        WebSocketEventType.BLOCKS_SYNC.getMessage());
        sendAfterCommit(() -> messagingTemplate.convertAndSend("/topic/blocks/" + areaKey, message));
    }

    private AreaRange toAreaRange(int blockX, int blockY) {
        int areaX = Math.floorDiv(blockX, AREA_SIZE);
        int areaY = Math.floorDiv(blockY, AREA_SIZE);
        int minX = areaX * AREA_SIZE;
        int minY = areaY * AREA_SIZE;
        return new AreaRange(minX, minX + AREA_SIZE - 1, minY, minY + AREA_SIZE - 1);
    }

    private void sendAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private static class SyncState {
        private final String areaKey;
        private LocalDateTime lastSyncedAt;

        private SyncState(String areaKey, LocalDateTime lastSyncedAt) {
            this.areaKey = areaKey;
            this.lastSyncedAt = lastSyncedAt;
        }
    }

    private static class AreaRange {
        private final int minX;
        private final int maxX;
        private final int minY;
        private final int maxY;

        private AreaRange(int minX, int maxX, int minY, int maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
