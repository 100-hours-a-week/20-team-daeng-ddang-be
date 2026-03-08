package com.daengddang.daengdong_map.sse;

import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class AnalysisTaskSseService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAIL = "FAIL";

    private final Map<String, Map<String, SseEmitter>> emittersByTaskId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(AnalysisTaskDetailResponse detailResponse, long emitterTimeoutMs) {
        String taskId = detailResponse.getTaskId();
        SseEmitter emitter = new SseEmitter(emitterTimeoutMs);
        String subscriberId = UUID.randomUUID().toString();

        emittersByTaskId
                .computeIfAbsent(taskId, ignored -> new ConcurrentHashMap<>())
                .put(subscriberId, emitter);

        Runnable cleanup = () -> remove(taskId, subscriberId);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name(AnalysisTaskSseEventType.CONNECTED.eventName())
                    .data(Map.of(
                            "taskId", taskId,
                            "connectedAt", Instant.now().toString()
                    )));
            emitter.send(SseEmitter.event()
                    .name(AnalysisTaskSseEventType.STATUS.eventName())
                    .data(detailResponse));
        } catch (IOException ex) {
            log.debug("분석 작업 SSE 구독 초기 이벤트 전송에 실패했습니다. taskId={}, subscriberId={}",
                    taskId, subscriberId, ex);
            cleanup.run();
            emitter.completeWithError(ex);
        }

        if (isTerminal(detailResponse.getStatus())) {
            completeAll(taskId);
        }

        return emitter;
    }

    public void publishStatus(AnalysisTaskDetailResponse detailResponse) {
        String taskId = detailResponse.getTaskId();
        Map<String, SseEmitter> subscribers = emittersByTaskId.get(taskId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        subscribers.forEach((subscriberId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(AnalysisTaskSseEventType.STATUS.eventName())
                        .id(UUID.randomUUID().toString())
                        .data(detailResponse));
            } catch (IOException ex) {
                log.debug("분석 작업 SSE 상태 이벤트 전송에 실패했습니다. taskId={}, subscriberId={}",
                        taskId, subscriberId, ex);
                remove(taskId, subscriberId);
            }
        });

        if (isTerminal(detailResponse.getStatus())) {
            completeAll(taskId);
        }
    }

    @Scheduled(fixedDelayString = "${async.sse.heartbeat-interval-ms:15000}")
    public void publishHeartbeat() {
        if (emittersByTaskId.isEmpty()) {
            return;
        }
        String now = Instant.now().toString();
        emittersByTaskId.forEach((taskId, subscribers) -> {
            if (subscribers == null || subscribers.isEmpty()) {
                return;
            }
            subscribers.forEach((subscriberId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name(AnalysisTaskSseEventType.HEARTBEAT.eventName())
                            .data(Map.of("ts", now)));
                } catch (IOException ex) {
                    log.debug("분석 작업 SSE 하트비트 전송에 실패했습니다. taskId={}, subscriberId={}",
                            taskId, subscriberId, ex);
                    remove(taskId, subscriberId);
                }
            });
        });
    }

    private boolean isTerminal(String status) {
        return STATUS_SUCCESS.equals(status) || STATUS_FAIL.equals(status);
    }

    private void completeAll(String taskId) {
        Map<String, SseEmitter> subscribers = emittersByTaskId.remove(taskId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }
        subscribers.values().forEach(emitter -> {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        });
    }

    private void remove(String taskId, String subscriberId) {
        Map<String, SseEmitter> subscribers = emittersByTaskId.get(taskId);
        if (subscribers == null) {
            return;
        }
        subscribers.remove(subscriberId);
        if (subscribers.isEmpty()) {
            emittersByTaskId.remove(taskId);
        }
    }
}
