package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockOccupiedPayload;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockOccupyFailReason;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockOccupyFailedPayload;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockTakenPayload;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.service.BlockSyncService;
import com.daengddang.daengdong_map.websocket.RedisWebSocketBroadcaster;
import com.daengddang.daengdong_map.websocket.WebSocketDestinations;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalkEventPublisher {

    private final RedisWebSocketBroadcaster broadcaster;
    private final BlockSyncService blockSyncService;
    private final AfterCommitExecutor afterCommitExecutor;

    public void sendError(Long walkId, String message) {
        broadcaster.broadcast(WebSocketDestinations.walk(walkId), WebSocketMessage.error(message));
    }

    public void sendOccupyFailed(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime timestamp) {
        BlockOccupyFailedPayload failPayload =
                BlockOccupyFailedPayload.from(BlockOccupyFailReason.INSUFFICIENT_STAY_TIME);

        WebSocketMessage<BlockOccupyFailedPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCK_OCCUPY_FAILED, failPayload,
                        WebSocketEventType.BLOCK_OCCUPY_FAILED.getMessage());

        broadcaster.broadcast(WebSocketDestinations.walk(walkId), message);
        blockSyncService.syncBlocksOnAreaChange(walkId, blockX, blockY, areaKey, timestamp);
    }

    public void sendBlockOccupied(Long walkId, String blockId, Dog dog, LocalDateTime occupiedAt, String areaKey) {
        BlockOccupiedPayload payload = BlockOccupiedPayload.from(blockId, dog.getId(), dog.getName(), occupiedAt);
        WebSocketMessage<BlockOccupiedPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCK_OCCUPIED, payload,
                        WebSocketEventType.BLOCK_OCCUPIED.getMessage());
        afterCommitExecutor.sendAfterCommit(() -> {
            broadcaster.broadcast(WebSocketDestinations.walk(walkId), message);
            broadcaster.broadcast(WebSocketDestinations.blocks(areaKey), message);
        });
    }

    public void sendBlockTaken(Long walkId, String blockId, Long previousDogId, Long newDogId,
                                LocalDateTime takenAt, String areaKey) {
        BlockTakenPayload payload = BlockTakenPayload.from(blockId, previousDogId, newDogId, takenAt);
        WebSocketMessage<BlockTakenPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCK_TAKEN, payload,
                        WebSocketEventType.BLOCK_TAKEN.getMessage());
        afterCommitExecutor.sendAfterCommit(() -> {
            broadcaster.broadcast(WebSocketDestinations.walk(walkId), message);
            broadcaster.broadcast(WebSocketDestinations.blocks(areaKey), message);
        });
    }

    public void syncBlocks(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime timestamp) {
        blockSyncService.syncBlocks(walkId, blockX, blockY, areaKey, timestamp);
    }

    public void syncBlocksOnAreaChange(Long walkId, int blockX, int blockY, String areaKey, LocalDateTime timestamp) {
        blockSyncService.syncBlocksOnAreaChange(walkId, blockX, blockY, areaKey, timestamp);
    }
}
