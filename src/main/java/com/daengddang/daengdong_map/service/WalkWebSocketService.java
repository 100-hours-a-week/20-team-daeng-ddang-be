package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.util.*;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketErrorReason;
import com.daengddang.daengdong_map.dto.websocket.inbound.LocationUpdatePayload;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.service.cache.BlockCacheStore;

import java.time.LocalDateTime;
import java.security.Principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkWebSocketService {

    private final WalkSessionValidator walkSessionValidator;
    private final BlockOccupancyService blockOccupancyService;
    private final WalkEventPublisher walkEventPublisher;
    private final WalkPointWriter walkPointWriter;
    private final StayValidator stayValidator;
    private final BlockSyncService blockSyncService;
    private final BlockCacheStore blockCacheStore;

    @Transactional
    public void handleLocationUpdate(Long walkId, LocationUpdatePayload payload, Principal principal) {
        LocalDateTime timestamp = LocalDateTime.now();

        Walk walk = walkSessionValidator.getActiveWalkOrNull(walkId);
        if (walk == null) {
            walkEventPublisher.sendError(walkId, WebSocketErrorReason.INVALID_WALK_SESSION.getMessage());
            return;
        }

        if (!walkSessionValidator.isValidCoordinate(payload.getLat(), payload.getLng())) {
            walkEventPublisher.sendError(walkId, WebSocketErrorReason.INVALID_LOCATION.getMessage());
            return;
        }

        walkPointWriter.save(walk, payload.getLat(), payload.getLng(), timestamp);

        int blockX = BlockIdUtil.toBlockX(payload.getLat());
        int blockY = BlockIdUtil.toBlockY(payload.getLng());
        String blockId = BlockIdUtil.toBlockId(blockX, blockY);
        String areaKey = blockSyncService.toAreaKey(blockX, blockY);
        boolean staySatisfied = stayValidator.isStaySatisfied(walkId, blockId, timestamp);

        if (!staySatisfied) {
            walkEventPublisher.sendOccupyFailed(walkId, blockX, blockY, areaKey, timestamp);
            return;
        }

        BlockOccupancyResult result = blockOccupancyService.occupy(walk, blockX, blockY, timestamp);
        switch (result.type()) {
            case OCCUPIED -> {
                evictBlockCache(blockX, blockY);
                walkEventPublisher.sendBlockOccupied(walkId, blockId, walk.getDog(), timestamp, areaKey);
                walkEventPublisher.syncBlocks(walkId, blockX, blockY, areaKey, timestamp);
            }
            case TAKEN -> {
                evictBlockCache(blockX, blockY);
                walkEventPublisher.sendBlockTaken(
                        walkId,
                        blockId,
                        result.previousDogId(),
                        walk.getDog().getId(),
                        timestamp,
                        areaKey
                );
                walkEventPublisher.syncBlocks(walkId, blockX, blockY, areaKey, timestamp);
            }
            case ALREADY_OWNED -> walkEventPublisher.syncBlocksOnAreaChange(walkId, blockX, blockY, areaKey, timestamp);
        }
    }

    private void evictBlockCache(int blockX, int blockY) {
        long deleted = blockCacheStore.evictByChangedBlock(blockX, blockY);
        if (log.isDebugEnabled()) {
            log.debug(
                    "블록 점유 변경으로 캐시 무효화 완료 (Block cache invalidated on ownership change): blockX={}, blockY={}, deletedKeys={}",
                    blockX,
                    blockY,
                    deleted
            );
        }
    }
}
