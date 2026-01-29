package com.daengddang.daengdong_map.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BlockIdUtil {

    public static final double BLOCK_SIZE = 0.00072;
    private static final int COORDINATE_SCALE = 6;

    private BlockIdUtil() {
    }

    public static int toBlockX(double lat) {
        return (int) Math.floor(lat / BLOCK_SIZE);
    }

    public static int toBlockY(double lng) {
        return (int) Math.floor(lng / BLOCK_SIZE);
    }

    public static String toBlockId(int blockX, int blockY) {
        BigDecimal minLat = BigDecimal.valueOf(blockX)
                .multiply(BigDecimal.valueOf(BLOCK_SIZE))
                .setScale(COORDINATE_SCALE, RoundingMode.DOWN);

        BigDecimal minLng = BigDecimal.valueOf(blockY)
                .multiply(BigDecimal.valueOf(BLOCK_SIZE))
                .setScale(COORDINATE_SCALE, RoundingMode.DOWN);

        return "P_" + minLat.toPlainString() + "_" + minLng.toPlainString();
    }
}
