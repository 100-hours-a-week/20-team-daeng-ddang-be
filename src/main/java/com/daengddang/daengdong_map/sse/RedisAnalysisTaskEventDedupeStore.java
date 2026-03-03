package com.daengddang.daengdong_map.sse;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisAnalysisTaskEventDedupeStore implements AnalysisTaskEventDedupeStore {

    private static final String VALUE = "1";

    private final RedissonClient redissonClient;
    private final AsyncSseRedisProperties redisProperties;

    @Override
    public boolean tryMarkFirstProcessed(String eventId) {
        if (!redisProperties.isEnabled() || !redisProperties.isDedupeEnabled()) {
            return true;
        }
        if (eventId == null || eventId.isBlank()) {
            return true;
        }

        String key = redisProperties.getDedupeKeyPrefix() + ":" + eventId;
        try {
            RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            return bucket.trySet(VALUE, redisProperties.getDedupeTtlSeconds(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("분석 작업 SSE dedupe 체크에 실패했습니다. eventId={}", eventId, ex);
            return true;
        }
    }
}
