package com.daengddang.daengdong_map.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockCacheKeyFactory {

    private final BlockCacheProperties properties;
    private final CacheDefaultProperties defaultProperties;

    public String buildAreaKey(int areaX, int areaY) {
        return resolvePrefix(properties.getKey())
                + ":ax:" + areaX
                + ":ay:" + areaY;
    }

    private String resolvePrefix(String baseKey) {
        String version = properties.getKeyVersion() == null
                ? defaultProperties.getKeyVersion()
                : properties.getKeyVersion();
        if (version == null || version.isBlank()) {
            return baseKey;
        }
        return version + ":" + baseKey;
    }
}
