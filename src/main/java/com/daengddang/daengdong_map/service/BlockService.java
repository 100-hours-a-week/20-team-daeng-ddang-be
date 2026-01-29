package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.BlockIdUtil;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.block.Block;
import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockListResponse;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockResponse;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private static final double BLOCK_METERS = 80.0;

    private final BlockOwnershipRepository blockOwnershipRepository;

    @Transactional(readOnly = true)
    public NearbyBlockListResponse getNearbyBlocks(Double lat, Double lng, Integer radiusMeters) {
        if (!isValidInput(lat, lng, radiusMeters)) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        int baseX = BlockIdUtil.toBlockX(lat);
        int baseY = BlockIdUtil.toBlockY(lng);
        int range = toRange(radiusMeters);

        int minX = baseX - range;
        int maxX = baseX + range;
        int minY = baseY - range;
        int maxY = baseY + range;

        List<NearbyBlockResponse> blocks = blockOwnershipRepository
                .findAllByBlockRange(minX, maxX, minY, maxY)
                .stream()
                .map(this::toNearbyBlock)
                .toList();

        return NearbyBlockListResponse.from(blocks);
    }

    private boolean isValidInput(Double lat, Double lng, Integer radiusMeters) {
        if (lat == null || lng == null || radiusMeters == null) {
            return false;
        }
        if (!Double.isFinite(lat) || !Double.isFinite(lng)) {
            return false;
        }
        return radiusMeters > 0;
    }

    private int toRange(int radiusMeters) {
        return (int) Math.ceil(radiusMeters / BLOCK_METERS);
    }

    private NearbyBlockResponse toNearbyBlock(BlockOwnership ownership) {
        Block block = ownership.getBlock();
        String blockId = BlockIdUtil.toBlockId(block.getX(), block.getY());
        return NearbyBlockResponse.from(
                blockId,
                ownership.getDog().getId(),
                ownership.getAcquiredAt()
        );
    }
}
