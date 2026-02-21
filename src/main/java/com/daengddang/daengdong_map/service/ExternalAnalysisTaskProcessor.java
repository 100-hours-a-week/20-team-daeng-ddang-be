package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.healthcare.HealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import com.daengddang.daengdong_map.repository.ExpressionRepository;
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
        ExternalAnalysisTask task = externalAnalysisTaskRepository.findWithContextByTaskId(taskId)
                .orElse(null);
        if (task == null) {
            log.warn("외부 분석 작업을 찾을 수 없습니다. taskId={}", taskId);
            return;
        }

        if (!externalAnalysisTaskStateService.markRunningIfPending(taskId)) {
            log.info("외부 분석 작업 상태 전이 생략(이미 처리 중/완료). taskId={}", taskId);
            return;
        }

        try {
            TaskResultRef resultRef = execute(task);
            externalAnalysisTaskStateService.markSuccessIfRunning(taskId, resultRef.resultType(), resultRef.resultId());
            log.info("외부 분석 작업 처리 성공. taskId={}, type={}", taskId, task.getType());
        } catch (BaseException ex) {
            String code = ex.getErrorCode().name();
            externalAnalysisTaskStateService.markFail(taskId, code, ex.getMessage());
            log.warn("외부 분석 작업 처리 실패. taskId={}, type={}, errorCode={}",
                    taskId, task.getType(), code);
        } catch (Exception ex) {
            externalAnalysisTaskStateService.markFail(taskId, ErrorCode.INTERNAL_SERVER_ERROR.name(), ex.getMessage());
            log.error("외부 분석 작업 처리 중 예외. taskId={}, type={}", taskId, task.getType(), ex);
        }
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
