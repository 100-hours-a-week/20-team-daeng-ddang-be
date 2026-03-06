package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockListResponse;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockResponse;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.projection.BlockOwnershipView;
import com.daengddang.daengdong_map.service.cache.BlockCacheMetrics;
import com.daengddang.daengdong_map.service.cache.BlockCachePolicy;
import com.daengddang.daengdong_map.service.cache.BlockCacheStore;
import com.daengddang.daengdong_map.util.BlockIdUtil;
import com.daengddang.daengdong_map.util.BlockOwnershipMapper;
import com.daengddang.daengdong_map.util.CoordinateValidator;
import java.util.ArrayList;
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

        int minX = baseX - range;
        int maxX = baseX + range;
        int minY = baseY - range;
        int maxY = baseY + range;

        int minAreaX = blockCachePolicy.toAreaX(minX);
        int maxAreaX = blockCachePolicy.toAreaX(maxX);
        int minAreaY = blockCachePolicy.toAreaY(minY);
        int maxAreaY = blockCachePolicy.toAreaY(maxY);

        List<NearbyBlockResponse> merged = new ArrayList<>();
        for (int areaX = minAreaX; areaX <= maxAreaX; areaX++) {
            for (int areaY = minAreaY; areaY <= maxAreaY; areaY++) {
                Optional<NearbyBlockListResponse> cachedArea = blockCacheStore.getArea(areaX, areaY);
                if (cachedArea.isPresent()) {
                    merged.addAll(cachedArea.get().getBlocks());
                    continue;
                }
                merged.addAll(loadAreaFromDbAndCache(areaX, areaY));
            }
        }

        List<NearbyBlockResponse> blocks = merged.stream()
                .filter(item -> isWithinRange(item.getBlockId(), minX, maxX, minY, maxY))
                .toList();

        if (log.isDebugEnabled()) {
            log.debug(
                    "블록 area cache 조회 완료 (Block area cache resolved): areaX={}~{}, areaY={}~{}, minX={}, maxX={}, minY={}, maxY={}, blockCount={}",
                    minAreaX,
                    maxAreaX,
                    minAreaY,
                    maxAreaY,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    blocks.size()
            );
        }
        return NearbyBlockListResponse.from(blocks);
    }

    private List<NearbyBlockResponse> loadAreaFromDbAndCache(int areaX, int areaY) {
        BlockCachePolicy.AreaRange areaRange = blockCachePolicy.toAreaRange(areaX, areaY);
        blockCacheMetrics.recordDbLoad();
        List<NearbyBlockResponse> blocks = blockOwnershipRepository
                .findAllByBlockRange(areaRange.minX(), areaRange.maxX(), areaRange.minY(), areaRange.maxY())
                .stream()
                .map(this::toNearbyBlock)
                .toList();
        blockCacheStore.putArea(areaX, areaY, NearbyBlockListResponse.from(blocks));
        return blocks;
    }

    private boolean isWithinRange(String blockId, int minX, int maxX, int minY, int maxY) {
        int[] xy = parseBlockCoordinates(blockId);
        if (xy == null) {
            return false;
        }
        return xy[0] >= minX && xy[0] <= maxX && xy[1] >= minY && xy[1] <= maxY;
    }

    private int[] parseBlockCoordinates(String blockId) {
        if (blockId == null || !blockId.startsWith("P_")) {
            return null;
        }
        String body = blockId.substring(2);
        String[] split = body.split("_", 2);
        if (split.length != 2) {
            return null;
        }
        try {
            double minLat = Double.parseDouble(split[0]);
            double minLng = Double.parseDouble(split[1]);
            return new int[]{BlockIdUtil.toBlockX(minLat), BlockIdUtil.toBlockY(minLng)};
        } catch (NumberFormatException e) {
            return null;
        }
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
