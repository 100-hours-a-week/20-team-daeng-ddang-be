package com.daengddang.daengdong_map.service.cache;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockCachePolicy {

    private static final double BLOCK_METERS = 80.0;
    public static final int AREA_SIZE = 13;

    private final CacheDefaultProperties defaultProperties;
    private final BlockCacheProperties properties;
    private final BlockCacheKeyFactory blockCacheKeyFactory;

    public long resolveTtlSeconds() {
        long base = properties.getTtlSeconds() == null
                ? defaultProperties.getTtlSeconds()
                : properties.getTtlSeconds();
        int jitterPercent = properties.getJitterPercent() == null
                ? defaultProperties.getJitterPercent()
                : properties.getJitterPercent();
        jitterPercent = Math.clamp(jitterPercent, 0, 100);
        if (jitterPercent == 0 || base <= 0) {
            return Math.clamp(base, 1L, Long.MAX_VALUE);
        }
        double ratio = jitterPercent / 100.0;
        double min = base * (1 - ratio);
        double max = base * (1 + ratio);
        long ttl = Math.round(ThreadLocalRandom.current().nextDouble(min, max));
        return Math.clamp(ttl, 1L, Long.MAX_VALUE);
    }

    public int toRange(int radiusMeters) {
        return (int) Math.ceil(radiusMeters / BLOCK_METERS);
    }

    public int toAreaX(int blockX) {
        return Math.floorDiv(blockX, AREA_SIZE);
    }

    public int toAreaY(int blockY) {
        return Math.floorDiv(blockY, AREA_SIZE);
    }

    public Set<String> buildTouchedAreaKeys(int minX, int maxX, int minY, int maxY) {
        int minAreaX = toAreaX(minX);
        int maxAreaX = toAreaX(maxX);
        int minAreaY = toAreaY(minY);
        int maxAreaY = toAreaY(maxY);
        Set<String> keys = new LinkedHashSet<>();
        for (int areaX = minAreaX; areaX <= maxAreaX; areaX++) {
            for (int areaY = minAreaY; areaY <= maxAreaY; areaY++) {
                keys.add(blockCacheKeyFactory.buildAreaKey(areaX, areaY));
            }
        }
        return keys;
    }

    public Set<String> buildInvalidateKeys(int changedBlockX, int changedBlockY) {
        Set<String> keys = new LinkedHashSet<>();
        keys.add(blockCacheKeyFactory.buildAreaKey(toAreaX(changedBlockX), toAreaY(changedBlockY)));
        return keys;
    }

    public AreaRange toAreaRange(int areaX, int areaY) {
        int minX = areaX * AREA_SIZE;
        int minY = areaY * AREA_SIZE;
        return new AreaRange(minX, minX + AREA_SIZE - 1, minY, minY + AREA_SIZE - 1);
    }

    public record AreaRange(int minX, int maxX, int minY, int maxY) {
    }
}
