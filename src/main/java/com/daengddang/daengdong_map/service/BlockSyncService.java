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
    private static final int AREA_CACHE_RANGE = AREA_SIZE / 2;
    private static final long SYNC_MIN_INTERVAL_SECONDS = 2;

    private final BlockOwnershipRepository blockOwnershipRepository;
    private final BlockCacheStore blockCacheStore;
    private final BlockCacheMetrics blockCacheMetrics;
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
        int baseX = range.minX + AREA_CACHE_RANGE;
        int baseY = range.minY + AREA_CACHE_RANGE;
        Optional<NearbyBlockListResponse> cached = blockCacheStore.getNearby(baseX, baseY, AREA_CACHE_RANGE);

        List<BlockSyncEntry> entries = cached
                .map(this::toSyncEntries)
                .orElseGet(() -> loadEntriesFromDbAndCache(range, baseX, baseY));

        BlocksSyncPayload payload = BlocksSyncPayload.from(entries);
        WebSocketMessage<BlocksSyncPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCKS_SYNC, payload,
                        WebSocketEventType.BLOCKS_SYNC.getMessage());
        afterCommitExecutor.sendAfterCommit(() ->
                broadcaster.broadcast(WebSocketDestinations.blocks(areaKey), message));
    }

    private List<BlockSyncEntry> loadEntriesFromDbAndCache(AreaRange range, int baseX, int baseY) {
        blockCacheMetrics.recordDbLoad();
        List<BlockOwnershipView> ownerships = blockOwnershipRepository.findAllByBlockRange(
                range.minX, range.maxX, range.minY, range.maxY
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
        blockCacheStore.putNearby(baseX, baseY, AREA_CACHE_RANGE, NearbyBlockListResponse.from(nearbyBlocks));
        return entries;
    }

    private List<BlockSyncEntry> toSyncEntries(NearbyBlockListResponse response) {
        return response.getBlocks().stream()
                .map(item -> BlockSyncEntry.from(item.getBlockId(), item.getDogId()))
                .toList();
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
