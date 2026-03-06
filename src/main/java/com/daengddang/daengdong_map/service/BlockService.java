package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockListResponse;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockResponse;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.projection.BlockOwnershipView;
import com.daengddang.daengdong_map.service.cache.BlockCacheMetrics;
import com.daengddang.daengdong_map.service.cache.BlockCacheKeyFactory;
import com.daengddang.daengdong_map.service.cache.BlockCachePolicy;
import com.daengddang.daengdong_map.service.cache.BlockCacheStore;
import com.daengddang.daengdong_map.util.BlockIdUtil;
import com.daengddang.daengdong_map.util.BlockOwnershipMapper;
import com.daengddang.daengdong_map.util.CoordinateValidator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockOwnershipRepository blockOwnershipRepository;
    private final BlockCacheKeyFactory blockCacheKeyFactory;
    private final BlockCachePolicy blockCachePolicy;
    private final BlockCacheStore blockCacheStore;
    private final BlockCacheMetrics blockCacheMetrics;

    @Transactional(readOnly = true)
    public NearbyBlockListResponse getNearbyBlocks(Double lat, Double lng, Integer radiusMeters) {
        if (!CoordinateValidator.isValidLatLng(lat, lng)
                || !CoordinateValidator.isValidRadius(radiusMeters)) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        int baseX = BlockIdUtil.toBlockX(lat);
        int baseY = BlockIdUtil.toBlockY(lng);
        int range = blockCachePolicy.toRange(radiusMeters);
        String cacheKey = blockCacheKeyFactory.buildNearbyListKey(baseX, baseY, range);

        int minX = baseX - range;
        int maxX = baseX + range;
        int minY = baseY - range;
        int maxY = baseY + range;

        Optional<NearbyBlockListResponse> cached = blockCacheStore.getNearby(baseX, baseY, range);
        if (cached.isPresent()) {
            return cached.get();
        }

        blockCacheMetrics.recordDbLoad();
        List<NearbyBlockResponse> blocks = blockOwnershipRepository
                .findAllByBlockRange(minX, maxX, minY, maxY)
                .stream()
                .map(this::toNearbyBlock)
                .toList();

        NearbyBlockListResponse response = NearbyBlockListResponse.from(blocks);
        blockCacheStore.putNearby(baseX, baseY, range, response);

        if (log.isDebugEnabled()) {
            log.debug(
                    "블록 조회 캐시 미스 후 DB 조회 완료 (Block cache miss -> DB load): key={}, minX={}, maxX={}, minY={}, maxY={}",
                    cacheKey,
                    minX,
                    maxX,
                    minY,
                    maxY
            );
        }
        return response;
    }

    private NearbyBlockResponse toNearbyBlock(BlockOwnershipView ownership) {
        String blockId = BlockOwnershipMapper.toBlockId(ownership);
        return NearbyBlockResponse.from(
                blockId,
                BlockOwnershipMapper.toOwnerDogId(ownership),
                BlockOwnershipMapper.toAcquiredAt(ownership)
        );
    }
}
