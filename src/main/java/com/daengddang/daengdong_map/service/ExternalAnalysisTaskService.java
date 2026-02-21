package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskAcceptedResponse;
import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import com.daengddang.daengdong_map.event.ExternalAnalysisTaskCreatedEvent;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import com.daengddang.daengdong_map.repository.MissionUploadRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAnalysisTaskService {

    private final AccessValidator accessValidator;
    private final MissionUploadRepository missionUploadRepository;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AnalysisTaskAcceptedResponse createMissionTask(Long userId, Long walkId) {
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        if (walk.getStatus() != WalkStatus.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        if (missionUploadRepository.findAllByWalk(walk).isEmpty()) {
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return createOrReuseWalkTask(walk, ExternalAnalysisTaskType.MISSION, null);
    }

    @Transactional
    public AnalysisTaskAcceptedResponse createExpressionTask(Long userId, Long walkId, ExpressionAnalyzeRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        return createOrReuseWalkTask(walk, ExternalAnalysisTaskType.EXPRESSION, dto.getVideoUrl());
    }

    @Transactional
    public AnalysisTaskAcceptedResponse createHealthcareTask(Long userId, HealthcareAnalyzeRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        Dog dog = accessValidator.getDogOrThrow(userId);
        return createOrReuseDogTask(dog, ExternalAnalysisTaskType.HEALTHCARE, dto.getVideoUrl());
    }

    @Transactional(readOnly = true)
    public AnalysisTaskDetailResponse getTask(Long userId, Long walkId, String taskId) {
        accessValidator.getOwnedWalkOrThrow(userId, walkId);
        ExternalAnalysisTask task = externalAnalysisTaskRepository.findByWalkIdAndTaskId(walkId, taskId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
        return AnalysisTaskDetailResponse.from(task);
    }

    @Transactional(readOnly = true)
    public AnalysisTaskDetailResponse getHealthcareTask(Long userId, String taskId) {
        Dog dog = accessValidator.getDogOrThrow(userId);
        ExternalAnalysisTask task = externalAnalysisTaskRepository.findByDogIdAndTaskId(dog.getId(), taskId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
        return AnalysisTaskDetailResponse.from(task);
    }

    private AnalysisTaskAcceptedResponse createOrReuseWalkTask(
            Walk walk,
            ExternalAnalysisTaskType type,
            String videoUrl
    ) {
        try {
            ExternalAnalysisTask saved = externalAnalysisTaskRepository.saveAndFlush(
                    ExternalAnalysisTask.builder()
                            .taskId(UUID.randomUUID().toString())
                            .type(type)
                            .status(ExternalAnalysisTaskStatus.PENDING)
                            .videoUrl(videoUrl)
                            .walk(walk)
                            .dog(walk.getDog())
                            .build()
            );
            eventPublisher.publishEvent(new ExternalAnalysisTaskCreatedEvent(saved.getTaskId(), saved.getType()));
            return AnalysisTaskAcceptedResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            ExternalAnalysisTask existing = externalAnalysisTaskRepository
                    .findLatestActiveByWalkIdAndType(walk.getId(), type)
                    .orElseThrow(() -> ex);
            return AnalysisTaskAcceptedResponse.from(existing);
        }
    }

    private AnalysisTaskAcceptedResponse createOrReuseDogTask(
            Dog dog,
            ExternalAnalysisTaskType type,
            String videoUrl
    ) {
        try {
            ExternalAnalysisTask saved = externalAnalysisTaskRepository.saveAndFlush(
                    ExternalAnalysisTask.builder()
                            .taskId(UUID.randomUUID().toString())
                            .type(type)
                            .status(ExternalAnalysisTaskStatus.PENDING)
                            .videoUrl(videoUrl)
                            .dog(dog)
                            .build()
            );
            eventPublisher.publishEvent(new ExternalAnalysisTaskCreatedEvent(saved.getTaskId(), saved.getType()));
            return AnalysisTaskAcceptedResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            ExternalAnalysisTask existing = externalAnalysisTaskRepository
                    .findLatestActiveByDogIdAndType(dog.getId(), type)
                    .orElseThrow(() -> ex);
            return AnalysisTaskAcceptedResponse.from(existing);
        }
    }
}
