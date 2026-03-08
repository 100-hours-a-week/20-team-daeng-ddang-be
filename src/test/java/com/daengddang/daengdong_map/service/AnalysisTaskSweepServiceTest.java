package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.analysis.AnalysisSweepProperties;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisTaskSweepServiceTest {

    @Mock
    private ExternalAnalysisTaskRepository externalAnalysisTaskRepository;

    @Mock
    private ExternalAnalysisTaskStateService externalAnalysisTaskStateService;

    private AnalysisSweepProperties properties;
    private AnalysisTaskSweepService analysisTaskSweepService;

    @BeforeEach
    void setUp() {
        properties = new AnalysisSweepProperties();
        properties.setPendingTimeoutSeconds(120);
        properties.setRunningTimeoutSeconds(300);
        properties.setBatchSize(2);
        analysisTaskSweepService = new AnalysisTaskSweepService(
                properties,
                externalAnalysisTaskRepository,
                externalAnalysisTaskStateService
        );
    }

    @Test
    void sweep_returnsZeroWhenNoStaleTasks() {
        when(externalAnalysisTaskRepository.findStalePendingBatch(any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of());
        when(externalAnalysisTaskRepository.findStaleRunningBatch(any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of());

        AnalysisTaskSweepService.SweepResult result = analysisTaskSweepService.sweep();

        assertThat(result.pendingSwept()).isEqualTo(0);
        assertThat(result.runningSwept()).isEqualTo(0);
        assertThat(result.totalSwept()).isEqualTo(0);
        verify(externalAnalysisTaskStateService, never()).markFail(any(), any(), any());
    }

    @Test
    void sweep_marksFailForStalePendingAndRunningTasks() {
        ExternalAnalysisTask pendingTask = mockTask("pending-task-id");
        ExternalAnalysisTask runningTask = mockTask("running-task-id");

        when(externalAnalysisTaskRepository.findStalePendingBatch(any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(pendingTask));
        when(externalAnalysisTaskRepository.findStaleRunningBatch(any(LocalDateTime.class), eq(2)))
                .thenReturn(List.of(runningTask));

        AnalysisTaskSweepService.SweepResult result = analysisTaskSweepService.sweep();

        assertThat(result.pendingSwept()).isEqualTo(1);
        assertThat(result.runningSwept()).isEqualTo(1);
        assertThat(result.totalSwept()).isEqualTo(2);

        verify(externalAnalysisTaskStateService).markFail(
                eq("pending-task-id"),
                eq("ANALYSIS_TASK_SWEEP_PENDING_TIMEOUT"),
                any(String.class)
        );
        verify(externalAnalysisTaskStateService).markFail(
                eq("running-task-id"),
                eq("ANALYSIS_TASK_SWEEP_RUNNING_TIMEOUT"),
                any(String.class)
        );
    }

    @Test
    void sweep_usesConfiguredBatchSize() {
        properties.setBatchSize(5);
        analysisTaskSweepService = new AnalysisTaskSweepService(
                properties,
                externalAnalysisTaskRepository,
                externalAnalysisTaskStateService
        );

        when(externalAnalysisTaskRepository.findStalePendingBatch(any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of());
        when(externalAnalysisTaskRepository.findStaleRunningBatch(any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of());

        analysisTaskSweepService.sweep();

        verify(externalAnalysisTaskRepository).findStalePendingBatch(any(LocalDateTime.class), eq(5));
        verify(externalAnalysisTaskRepository).findStaleRunningBatch(any(LocalDateTime.class), eq(5));
    }

    private ExternalAnalysisTask mockTask(String taskId) {
        ExternalAnalysisTask task = mock(ExternalAnalysisTask.class);
        when(task.getTaskId()).thenReturn(taskId);
        return task;
    }
}
