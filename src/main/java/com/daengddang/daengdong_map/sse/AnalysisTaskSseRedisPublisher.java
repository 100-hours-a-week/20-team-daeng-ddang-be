package com.daengddang.daengdong_map.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisTaskSseRedisPublisher {

    private static final String SCHEMA_VERSION = "v2";

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final AsyncSseRedisProperties redisProperties;

    public void publish(String taskId) {
        if (!redisProperties.isEnabled() || taskId == null || taskId.isBlank()) {
            return;
        }

        AnalysisTaskSseRedisEnvelope envelope = AnalysisTaskSseRedisEnvelope.builder()
                .sourceNodeId(redisProperties.getNodeId())
                .eventId(UUID.randomUUID().toString())
                .taskId(taskId)
                .occurredAt(Instant.now().toString())
                .schemaVersion(SCHEMA_VERSION)
                .build();

        try {
            String message = objectMapper.writeValueAsString(envelope);
            redissonClient.getTopic(redisProperties.getChannel(), StringCodec.INSTANCE).publish(message);
        } catch (JsonProcessingException ex) {
            log.warn("분석 작업 SSE Redis 발행 직렬화에 실패했습니다. taskId={}", taskId, ex);
        } catch (Exception ex) {
            log.warn("분석 작업 SSE Redis 발행에 실패했습니다. taskId={}, channel={}",
                    taskId, redisProperties.getChannel(), ex);
        }
    }
}
