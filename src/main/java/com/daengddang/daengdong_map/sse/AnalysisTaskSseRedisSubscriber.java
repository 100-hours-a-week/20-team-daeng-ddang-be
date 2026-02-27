package com.daengddang.daengdong_map.sse;

import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisTaskSseRedisSubscriber {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final AsyncSseRedisProperties redisProperties;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final AnalysisTaskSseService analysisTaskSseService;

    private Integer listenerId;

    @PostConstruct
    public void subscribe() {
        if (!redisProperties.isEnabled()) {
            return;
        }
        try {
            RTopic topic = redissonClient.getTopic(redisProperties.getChannel(), StringCodec.INSTANCE);
            listenerId = topic.addListener(String.class, (channel, message) -> handleMessage(message));
            log.info("분석 작업 SSE Redis 구독을 시작했습니다. channel={}, nodeId={}",
                    redisProperties.getChannel(), redisProperties.getNodeId());
        } catch (Exception ex) {
            log.warn("분석 작업 SSE Redis 구독 시작에 실패했습니다. channel={}, nodeId={}",
                    redisProperties.getChannel(), redisProperties.getNodeId(), ex);
        }
    }

    @PreDestroy
    public void unsubscribe() {
        if (!redisProperties.isEnabled() || listenerId == null) {
            return;
        }
        try {
            redissonClient.getTopic(redisProperties.getChannel(), StringCodec.INSTANCE).removeListener(listenerId);
            log.info("분석 작업 SSE Redis 구독을 종료했습니다. channel={}, nodeId={}",
                    redisProperties.getChannel(), redisProperties.getNodeId());
        } catch (Exception ex) {
            log.warn("분석 작업 SSE Redis 구독 종료에 실패했습니다. channel={}, nodeId={}",
                    redisProperties.getChannel(), redisProperties.getNodeId(), ex);
        }
    }

    private void handleMessage(String message) {
        try {
            AnalysisTaskSseRedisEnvelope envelope =
                    objectMapper.readValue(message, AnalysisTaskSseRedisEnvelope.class);

            if (redisProperties.getNodeId().equals(envelope.getSourceNodeId())) {
                return;
            }
            if (envelope.getTaskId() == null || envelope.getTaskId().isBlank()) {
                return;
            }

            externalAnalysisTaskRepository.findWithContextByTaskId(envelope.getTaskId())
                    .map(AnalysisTaskDetailResponse::from)
                    .ifPresent(analysisTaskSseService::publishStatus);

        } catch (Exception ex) {
            log.warn("분석 작업 SSE Redis 메시지 처리에 실패했습니다. channel={}, nodeId={}",
                    redisProperties.getChannel(), redisProperties.getNodeId(), ex);
        }
    }
}
