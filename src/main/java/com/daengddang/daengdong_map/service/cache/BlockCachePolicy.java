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

    public Set<String> buildInvalidateKeys(int changedBlockX, int changedBlockY) {
        int minRange = toRange(resolveMinInvalidateRadiusMeters());
        int maxRange = toRange(resolveMaxInvalidateRadiusMeters());
        Set<String> keys = new LinkedHashSet<>();
        for (int range = minRange; range <= maxRange; range++) {
            for (int baseX = changedBlockX - range; baseX <= changedBlockX + range; baseX++) {
                for (int baseY = changedBlockY - range; baseY <= changedBlockY + range; baseY++) {
                    keys.add(blockCacheKeyFactory.buildNearbyListKey(baseX, baseY, range));
                }
            }
        }
        return keys;
    }

    private int resolveMinInvalidateRadiusMeters() {
        return clampNonNegative(properties.getInvalidateMinRadiusMeters());
    }

    private int resolveMaxInvalidateRadiusMeters() {
        int min = resolveMinInvalidateRadiusMeters();
        int max = clampNonNegative(properties.getInvalidateMaxRadiusMeters());
        return Math.max(min, max);
    }

    private int clampNonNegative(Integer value) {
        if (value == null) {
            return 0;
        }
        return Math.clamp(value, 0, Integer.MAX_VALUE);
    }
}
