package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.BlockIdUtil;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketErrorReason;
import com.daengddang.daengdong_map.dto.websocket.inbound.LocationUpdatePayload;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockOccupiedPayload;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockOccupyFailReason;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockOccupyFailedPayload;
import com.daengddang.daengdong_map.dto.websocket.outbound.BlockTakenPayload;
import com.daengddang.daengdong_map.domain.block.Block;
import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.BlockRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;

import java.time.LocalDateTime;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class WalkWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WalkRepository walkRepository;
    private final BlockRepository blockRepository;
    private final BlockOwnershipRepository blockOwnershipRepository;
    private final WalkPointWriter walkPointWriter;
    private final StayValidator stayValidator;
    private final BlockSyncService blockSyncService;

    @Transactional
    public void handleLocationUpdate(Long walkId, LocationUpdatePayload payload, Principal principal) {
        // TODO: walk 상태/권한 검증 + 점유/탈취 로직
        LocalDateTime timestamp = LocalDateTime.now();

        Walk walk = walkRepository.findById(walkId)
                .orElse(null);

        if (walk == null || walk.getStatus() != WalkStatus.IN_PROGRESS) {
            sendError(walkId, WebSocketErrorReason.INVALID_WALK_SESSION.getMessage());
            return;
        }

        if (!isValidCoordinate(payload.getLat(), payload.getLng())) {
            sendError(walkId, WebSocketErrorReason.INVALID_LOCATION.getMessage());
            return;
        }

        walkPointWriter.save(walk, payload.getLat(), payload.getLng(), timestamp);

        int blockX = BlockIdUtil.toBlockX(payload.getLat());
        int blockY = BlockIdUtil.toBlockY(payload.getLng());
        String blockId = BlockIdUtil.toBlockId(blockX, blockY);
        String areaKey = blockSyncService.toAreaKey(blockX, blockY);
        boolean staySatisfied = stayValidator.isStaySatisfied(walkId, blockId, timestamp);

        if (!staySatisfied) {
            BlockOccupyFailedPayload failPayload =
                    BlockOccupyFailedPayload.from(BlockOccupyFailReason.INSUFFICIENT_STAY_TIME);

            WebSocketMessage<BlockOccupyFailedPayload> message =
                    new WebSocketMessage<>(WebSocketEventType.BLOCK_OCCUPY_FAILED, failPayload,
                            WebSocketEventType.BLOCK_OCCUPY_FAILED.getMessage());

            messagingTemplate.convertAndSend("/topic/walks/" + walkId, message);
            blockSyncService.syncBlocksOnAreaChange(walkId, blockX, blockY, areaKey, timestamp);
            return;
        }

        Dog dog = walk.getDog();
        blockRepository.insertIfNotExists(blockX, blockY);
        Block block = blockRepository.findByXAndY(blockX, blockY)
                .orElseThrow();

        BlockOwnership ownership = blockOwnershipRepository.findById(block.getId()).orElse(null);
        if (ownership == null) {
            BlockOwnership newOwnership = BlockOwnership.builder()
                    .block(block)
                    .dog(dog)
                    .acquiredAt(timestamp)
                    .lastPassedAt(timestamp)
                    .build();
            blockOwnershipRepository.save(newOwnership);
            sendBlockOccupied(walkId, blockId, dog, timestamp, areaKey);
            blockSyncService.syncBlocks(walkId, blockX, blockY, areaKey, timestamp);
            return;
        }

        if (ownership.getDog().getId().equals(dog.getId())) {
            ownership.updateLastPassedAt(timestamp);
            blockSyncService.syncBlocksOnAreaChange(walkId, blockX, blockY, areaKey, timestamp);
            return;
        }

        Long previousDogId = ownership.getDog().getId();
        ownership.updateOwner(dog, timestamp);
        sendBlockTaken(walkId, blockId, previousDogId, dog.getId(), timestamp, areaKey);
        blockSyncService.syncBlocks(walkId, blockX, blockY, areaKey, timestamp);
    }

    public void sendError(Long walkId, String message) {
        messagingTemplate.convertAndSend("/topic/walks/" + walkId, WebSocketMessage.error(message));
    }

    // blockId/좌표 변환은 BlockIdUtil에서 공통 처리한다.

    private void sendBlockOccupied(Long walkId, String blockId, Dog dog, LocalDateTime occupiedAt, String areaKey) {
        BlockOccupiedPayload payload = BlockOccupiedPayload.from(blockId, dog.getId(), dog.getName(), occupiedAt);
        WebSocketMessage<BlockOccupiedPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCK_OCCUPIED, payload,
                        WebSocketEventType.BLOCK_OCCUPIED.getMessage());
        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/topic/walks/" + walkId, message);
            messagingTemplate.convertAndSend("/topic/blocks/" + areaKey, message);
        });
    }

    private void sendBlockTaken(Long walkId, String blockId, Long previousDogId, Long newDogId,
                                LocalDateTime takenAt, String areaKey) {
        BlockTakenPayload payload = BlockTakenPayload.from(blockId, previousDogId, newDogId, takenAt);
        WebSocketMessage<BlockTakenPayload> message =
                new WebSocketMessage<>(WebSocketEventType.BLOCK_TAKEN, payload,
                        WebSocketEventType.BLOCK_TAKEN.getMessage());
        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/topic/walks/" + walkId, message);
            messagingTemplate.convertAndSend("/topic/blocks/" + areaKey, message);
        });
    }

    private boolean isValidCoordinate(double lat, double lng) {
        if (!Double.isFinite(lat) || !Double.isFinite(lng)) {
            return false;
        }
        return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0;
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

}
