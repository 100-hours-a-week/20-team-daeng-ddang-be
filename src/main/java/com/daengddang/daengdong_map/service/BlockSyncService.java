package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.util.BlockIdUtil;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockSyncEntry;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlocksSyncPayload;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.projection.BlockOwnershipView;
import com.daengddang.daengdong_map.websocket.RedisWebSocketBroadcaster;
import com.daengddang.daengdong_map.websocket.WebSocketDestinations;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import com.daengddang.daengdong_map.util.AfterCommitExecutor;
import com.daengddang.daengdong_map.util.WalkRuntimeStateRegistry;
import com.daengddang.daengdong_map.util.WalkRuntimeStateRegistry.SyncState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockSyncService {

    private static final int AREA_SIZE = 13;
    private static final long SYNC_MIN_INTERVAL_SECONDS = 2;

    private final BlockOwnershipRepository blockOwnershipRepository;
    private final RedisWebSocketBroadcaster broadcaster;
    private final WalkRuntimeStateRegistry stateRegistry;
    private final AfterCommitExecutor afterCommitExecutor;

    public String toAreaKey(int blockX, int blockY) {
        int areaX = Math.floorDiv(blockX, AREA_SIZE);
        int areaY = Math.floorDiv(blockY, AREA_SIZE);
        return areaX + "_" + areaY;
    }

    public void syncBlocks(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime now) {
        SyncState state = stateRegistry.getSyncState(walkId);
        if (state == null || !state.getAreaKey().equals(areaKey)) {
            stateRegistry.putSyncState(walkId, new SyncState(areaKey, now));
            sendBlocksSync(blockX, blockY, areaKey);
            return;
        }

        Duration since = Duration.between(state.getLastSyncedAt(), now);
        if (since.getSeconds() < SYNC_MIN_INTERVAL_SECONDS) {
            return;
        }

        state.recordLastSyncedAt(now);
        stateRegistry.putSyncState(walkId, state);
        sendBlocksSync(blockX, blockY, areaKey);
    }

    public void syncBlocksOnAreaChange(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime now) {
        SyncState state = stateRegistry.getSyncState(walkId);
        if (state == null || !state.getAreaKey().equals(areaKey)) {
            stateRegistry.putSyncState(walkId, new SyncState(areaKey, now));
            sendBlocksSync(blockX, blockY, areaKey);
        }
    }

    private void sendBlocksSync(int blockX, int blockY, String areaKey) {
        AreaRange range = toAreaRange(blockX, blockY);
        List<BlockSyncEntry> entries = blockOwnershipRepository.findAllByBlockRange(
                        range.minX, range.maxX, range.minY, range.maxY
                ).stream()
                .map(this::toBlockSyncEntry)
                .toList();

        BlocksSyncPayload payload = BlocksSyncPayload.from(entries);
        WebSocketMessage<BlocksSyncPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCKS_SYNC, payload,
                        WebSocketEventType.BLOCKS_SYNC.getMessage());
        afterCommitExecutor.sendAfterCommit(() ->
                broadcaster.broadcast(WebSocketDestinations.blocks(areaKey), message));
    }

    private AreaRange toAreaRange(int blockX, int blockY) {
        int areaX = Math.floorDiv(blockX, AREA_SIZE);
        int areaY = Math.floorDiv(blockY, AREA_SIZE);
        int minX = areaX * AREA_SIZE;
        int minY = areaY * AREA_SIZE;
        return new AreaRange(minX, minX + AREA_SIZE - 1, minY, minY + AREA_SIZE - 1);
    }

    private BlockSyncEntry toBlockSyncEntry(BlockOwnershipView ownership) {
        return BlockSyncEntry.from(
                BlockIdUtil.toBlockId(ownership.getBlockX(), ownership.getBlockY()),
                ownership.getDogId()
        );
    }

    private record AreaRange(int minX, int maxX, int minY, int maxY) {
    }
}
