package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskAcceptedResponse;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import com.daengddang.daengdong_map.repository.MissionUploadRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAnalysisTaskService {

    private final AccessValidator accessValidator;
    private final MissionUploadRepository missionUploadRepository;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;

    @Transactional
    public AnalysisTaskAcceptedResponse createMissionTask(Long userId, Long walkId) {
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        if (walk.getStatus() != WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        if (missionUploadRepository.findAllByWalk(walk).isEmpty()) {
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return createOrReuseTask(walk, ExternalAnalysisTaskType.MISSION, null);
    }

    @Transactional
    public AnalysisTaskAcceptedResponse createExpressionTask(Long userId, Long walkId, ExpressionAnalyzeRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        return createOrReuseTask(walk, ExternalAnalysisTaskType.EXPRESSION, dto.getVideoUrl());
    }

    @Transactional
    public AnalysisTaskAcceptedResponse createHealthcareTask(Long userId, Long walkId, HealthcareAnalyzeRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        return createOrReuseTask(walk, ExternalAnalysisTaskType.HEALTHCARE, dto.getVideoUrl());
    }

    @Transactional(readOnly = true)
    public AnalysisTaskDetailResponse getTask(Long userId, Long walkId, String taskId) {
        accessValidator.getOwnedWalkOrThrow(userId, walkId);
        ExternalAnalysisTask task = externalAnalysisTaskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!task.getWalk().getId().equals(walkId)) {
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return AnalysisTaskDetailResponse.from(task);
    }

    private AnalysisTaskAcceptedResponse createOrReuseTask(
            Walk walk,
            ExternalAnalysisTaskType type,
            String videoUrl
    ) {
        Optional<ExternalAnalysisTask> latest = externalAnalysisTaskRepository.findLatestByWalkIdAndType(walk.getId(), type);
        if (latest.isPresent()) {
            ExternalAnalysisTask current = latest.get();
            if (current.getStatus() == ExternalAnalysisTaskStatus.PENDING
                    || current.getStatus() == ExternalAnalysisTaskStatus.RUNNING) {
                return AnalysisTaskAcceptedResponse.from(current);
            }
        }

        ExternalAnalysisTask saved = externalAnalysisTaskRepository.save(
                ExternalAnalysisTask.builder()
                        .taskId(UUID.randomUUID().toString())
                        .type(type)
                        .status(ExternalAnalysisTaskStatus.PENDING)
                        .videoUrl(videoUrl)
                        .walk(walk)
                        .build()
        );
        return AnalysisTaskAcceptedResponse.from(saved);
    }
}
