package com.daengddang.daengdong_map.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisJsonBucketCacheHelper {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> read(String key,
                                Class<T> payloadType,
                                Runnable onMiss,
                                Runnable onHit) throws Exception {
        String cachedJson = redissonClient.<String>getBucket(key, StringCodec.INSTANCE).get();
        if (cachedJson == null || cachedJson.isBlank()) {
            onMiss.run();
            return Optional.empty();
        }

        T payload = objectMapper.readValue(cachedJson, payloadType);
        onHit.run();
        return Optional.of(payload);
    }

    public void writeAsync(String key,
                           Object payload,
                           long ttlSeconds,
                           Consumer<Throwable> onWriteError) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        redissonClient.<String>getBucket(key, StringCodec.INSTANCE)
                .setAsync(json, ttlSeconds, TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        onWriteError.accept(throwable);
                    }
                });
    }
}
