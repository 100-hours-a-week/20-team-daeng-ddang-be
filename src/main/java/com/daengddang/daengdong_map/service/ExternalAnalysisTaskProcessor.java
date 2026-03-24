package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.AnalysisTaskAlreadyFailedException;
import com.daengddang.daengdong_map.common.exception.AnalysisTaskAlreadyRunningException;
import com.daengddang.daengdong_map.common.exception.AnalysisTaskAlreadySucceededException;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.healthcare.HealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import com.daengddang.daengdong_map.repository.ExpressionRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalAnalysisTaskProcessor {

    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final ExternalAnalysisTaskStateService externalAnalysisTaskStateService;
    private final MissionJudgeService missionJudgeService;
    private final ExpressionAnalyzeService expressionAnalyzeService;
    private final HealthcareService healthcareService;
    private final ExpressionRepository expressionRepository;

    public void process(String taskId) {
        try {
            processOrThrow(taskId);
        } catch (BaseException ex) {
            String code = ex.getErrorCode().name();
            log.warn("외부 분석 작업 처리 실패(BaseException). taskId={}, errorCode={}, message={}",
                    taskId, code, ex.getMessage(), ex);
            externalAnalysisTaskStateService.markFail(taskId, code, ex.getErrorCode().getMessage());
        } catch (Exception ex) {
            log.error("외부 분석 작업 처리 중 예외. taskId={}", taskId, ex);
            externalAnalysisTaskStateService.markFail(
                    taskId,
                    ErrorCode.INTERNAL_SERVER_ERROR.name(),
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
            );
        }
    }

    public void processOrThrow(String taskId) {
        Instant startedAt = Instant.now();
        ExternalAnalysisTask task = externalAnalysisTaskRepository.findWithContextByTaskId(taskId)
                .orElse(null);
        if (task == null) {
            log.warn("외부 분석 작업을 찾을 수 없습니다. taskId={}", taskId);
            return;
        }

        log.info("외부 분석 작업 처리 시작. taskId={}, type={}, requestedAt={}, walkId={}, dogId={}",
                taskId,
                task.getType(),
                task.getRequestedAt(),
                task.getWalk() == null ? null : task.getWalk().getId(),
                task.getDog() == null ? null : task.getDog().getId());

        if (!externalAnalysisTaskStateService.markRunningIfPending(taskId)) {
            ExternalAnalysisTaskStatus currentStatus = externalAnalysisTaskRepository.findStatusByTaskId(taskId)
                    .orElse(task.getStatus());
            switch (currentStatus) {
                case FAIL -> {
                    log.warn("외부 분석 작업이 이미 FAIL 상태라 재전달 메시지를 DLQ로 보냅니다. taskId={}", taskId);
                    throw new AnalysisTaskAlreadyFailedException(taskId);
                }
                case SUCCESS -> {
                    log.info("외부 분석 작업이 이미 SUCCESS 상태라 중복 메시지를 건너뜁니다. taskId={}", taskId);
                    throw new AnalysisTaskAlreadySucceededException(taskId);
                }
                case RUNNING -> {
                    log.warn("외부 분석 작업이 이미 RUNNING 상태라 중복 실행을 건너뜁니다. taskId={}", taskId);
                    throw new AnalysisTaskAlreadyRunningException(taskId);
                }
                case PENDING -> {
                    log.warn("외부 분석 작업이 아직 PENDING 상태라 이번 메시지 처리를 건너뜁니다. taskId={}", taskId);
                    return;
                }
            }
        }

        TaskResultRef resultRef = execute(task);
        externalAnalysisTaskStateService.markSuccessIfRunning(taskId, resultRef.resultType(), resultRef.resultId());
        log.info("외부 분석 작업 처리 성공. taskId={}, type={}, durationMs={}, resultType={}, resultId={}",
                taskId, task.getType(), Duration.between(startedAt, Instant.now()).toMillis(),
                resultRef.resultType(), resultRef.resultId());
    }

    private TaskResultRef execute(ExternalAnalysisTask task) {
        Long userId = task.getDog().getUser().getId();
        ExternalAnalysisTaskType type = task.getType();
        Long walkId = task.getWalk() == null ? null : task.getWalk().getId();

        switch (type) {
            case MISSION -> {
                missionJudgeService.judge(userId, requireWalkId(task));
                return new TaskResultRef("MISSION", String.valueOf(walkId));
            }
            case EXPRESSION -> {
                expressionAnalyzeService.analyze(
                        userId,
                        requireWalkId(task),
                        ExpressionAnalyzeRequest.of(requireVideoUrl(task))
                );
                Long expressionId = expressionRepository.findByWalk(task.getWalk())
                        .map(expression -> expression.getId())
                        .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
                return new TaskResultRef("EXPRESSION", String.valueOf(expressionId));
            }
            case HEALTHCARE -> {
                HealthcareAnalyzeResponse response = healthcareService.analyze(
                        userId,
                        HealthcareAnalyzeRequest.of(requireVideoUrl(task))
                );
                return new TaskResultRef("HEALTHCARE", String.valueOf(response.getHealthcareId()));
            }
        }
        throw new BaseException(ErrorCode.INVALID_FORMAT);
    }

    private Long requireWalkId(ExternalAnalysisTask task) {
        if (task.getWalk() == null || task.getWalk().getId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return task.getWalk().getId();
    }

    private String requireVideoUrl(ExternalAnalysisTask task) {
        if (task.getVideoUrl() == null || task.getVideoUrl().isBlank()) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return task.getVideoUrl();
    }

    private record TaskResultRef(String resultType, String resultId) {
    }
}
