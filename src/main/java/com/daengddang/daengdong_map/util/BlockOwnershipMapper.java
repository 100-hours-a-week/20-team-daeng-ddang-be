package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.repository.projection.BlockOwnershipView;
import java.time.LocalDateTime;

public final class BlockOwnershipMapper {

    private BlockOwnershipMapper() {
    }

    public static String toBlockId(BlockOwnershipView ownership) {
        return BlockIdUtil.toBlockId(ownership.getBlockX(), ownership.getBlockY());
    }

    public static Long toOwnerDogId(BlockOwnershipView ownership) {
        return ownership.getDogId();
    }

    public static LocalDateTime toAcquiredAt(BlockOwnershipView ownership) {
        return ownership.getAcquiredAt();
    }
}
