package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.event.ExternalAnalysisTaskCreatedEvent;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAnalysisTaskCreationService {

    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final AnalysisTaskOutboxService analysisTaskOutboxService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalAnalysisTask createWalkTask(Walk walk, ExternalAnalysisTaskType type, String videoUrl) {
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
        analysisTaskOutboxService.saveTaskCreatedOutbox(saved);
        eventPublisher.publishEvent(new ExternalAnalysisTaskCreatedEvent(saved.getTaskId(), saved.getType()));
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalAnalysisTask createDogTask(Dog dog, ExternalAnalysisTaskType type, String videoUrl) {
        ExternalAnalysisTask saved = externalAnalysisTaskRepository.saveAndFlush(
                ExternalAnalysisTask.builder()
                        .taskId(UUID.randomUUID().toString())
                        .type(type)
                        .status(ExternalAnalysisTaskStatus.PENDING)
                        .videoUrl(videoUrl)
                        .dog(dog)
                        .build()
        );
        analysisTaskOutboxService.saveTaskCreatedOutbox(saved);
        eventPublisher.publishEvent(new ExternalAnalysisTaskCreatedEvent(saved.getTaskId(), saved.getType()));
        return saved;
    }
}
