package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.dto.response.block.NearbyBlockListResponse;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockResponse;
import com.daengddang.daengdong_map.util.BlockIdUtil;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockSyncEntry;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlocksSyncPayload;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.projection.BlockOwnershipView;
import com.daengddang.daengdong_map.service.cache.BlockCacheMetrics;
import com.daengddang.daengdong_map.service.cache.BlockCachePolicy;
import com.daengddang.daengdong_map.service.cache.BlockCacheStore;
import com.daengddang.daengdong_map.websocket.RedisWebSocketBroadcaster;
import com.daengddang.daengdong_map.websocket.WebSocketDestinations;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final BlockCacheStore blockCacheStore;
    private final BlockCacheMetrics blockCacheMetrics;
    private final BlockCachePolicy blockCachePolicy;
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
        int areaX = Math.floorDiv(blockX, AREA_SIZE);
        int areaY = Math.floorDiv(blockY, AREA_SIZE);
        Optional<NearbyBlockListResponse> cached = blockCacheStore.getArea(areaX, areaY);

        List<BlockSyncEntry> entries = cached
                .map(this::toSyncEntries)
                .orElseGet(() -> loadEntriesFromDbAndCache(areaX, areaY));

        BlocksSyncPayload payload = BlocksSyncPayload.from(entries);
        WebSocketMessage<BlocksSyncPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCKS_SYNC, payload,
                        WebSocketEventType.BLOCKS_SYNC.getMessage());
        afterCommitExecutor.sendAfterCommit(() ->
                broadcaster.broadcast(WebSocketDestinations.blocks(areaKey), message));
    }

    private List<BlockSyncEntry> loadEntriesFromDbAndCache(int areaX, int areaY) {
        BlockCachePolicy.AreaRange areaRange = blockCachePolicy.toAreaRange(areaX, areaY);
        blockCacheMetrics.recordDbLoad();
        List<BlockOwnershipView> ownerships = blockOwnershipRepository.findAllByBlockRange(
                areaRange.minX(), areaRange.maxX(), areaRange.minY(), areaRange.maxY()
        );
        List<BlockSyncEntry> entries = ownerships.stream()
                .map(this::toBlockSyncEntry)
                .toList();
        List<NearbyBlockResponse> nearbyBlocks = ownerships.stream()
                .map(ownership -> NearbyBlockResponse.from(
                        BlockIdUtil.toBlockId(ownership.getBlockX(), ownership.getBlockY()),
                        ownership.getDogId(),
                        ownership.getAcquiredAt()
                ))
                .toList();
        blockCacheStore.putArea(areaX, areaY, NearbyBlockListResponse.from(nearbyBlocks));
        return entries;
    }

    private List<BlockSyncEntry> toSyncEntries(NearbyBlockListResponse response) {
        return response.getBlocks().stream()
                .map(item -> BlockSyncEntry.from(item.getBlockId(), item.getDogId()))
                .toList();
    }

    private BlockSyncEntry toBlockSyncEntry(BlockOwnershipView ownership) {
        return BlockSyncEntry.from(
                BlockIdUtil.toBlockId(ownership.getBlockX(), ownership.getBlockY()),
                ownership.getDogId()
        );
    }
}
