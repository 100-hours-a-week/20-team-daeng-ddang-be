package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.analysis.AnalysisSweepProperties;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisTaskSweepService {

    private static final String SWEEP_PENDING_ERROR_CODE = "ANALYSIS_TASK_SWEEP_PENDING_TIMEOUT";
    private static final String SWEEP_RUNNING_ERROR_CODE = "ANALYSIS_TASK_SWEEP_RUNNING_TIMEOUT";

    private final AnalysisSweepProperties properties;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final ExternalAnalysisTaskStateService externalAnalysisTaskStateService;

    @Transactional(readOnly = true)
    public SweepResult sweep() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pendingCutoff = now.minusSeconds(properties.getPendingTimeoutSeconds());
        LocalDateTime runningCutoff = now.minusSeconds(properties.getRunningTimeoutSeconds());

        int pendingSwept = sweepPending(pendingCutoff);
        int runningSwept = sweepRunning(runningCutoff);

        return new SweepResult(pendingSwept, runningSwept, pendingCutoff, runningCutoff);
    }

    private int sweepPending(LocalDateTime cutoff) {
        List<ExternalAnalysisTask> stalePending = externalAnalysisTaskRepository
                .findStalePendingBatch(cutoff, properties.getBatchSize());
        for (ExternalAnalysisTask task : stalePending) {
            externalAnalysisTaskStateService.markFail(
                    task.getTaskId(),
                    SWEEP_PENDING_ERROR_CODE,
                    buildTimeoutMessage("PENDING", task.getTaskId(), cutoff)
            );
        }
        return stalePending.size();
    }

    private int sweepRunning(LocalDateTime cutoff) {
        List<ExternalAnalysisTask> staleRunning = externalAnalysisTaskRepository
                .findStaleRunningBatch(cutoff, properties.getBatchSize());
        for (ExternalAnalysisTask task : staleRunning) {
            externalAnalysisTaskStateService.markFail(
                    task.getTaskId(),
                    SWEEP_RUNNING_ERROR_CODE,
                    buildTimeoutMessage("RUNNING", task.getTaskId(), cutoff)
            );
        }
        return staleRunning.size();
    }

    private String buildTimeoutMessage(String status, String taskId, LocalDateTime cutoff) {
        return "sweep timeout status=" + status + ", taskId=" + taskId + ", cutoff=" + cutoff;
    }

    public record SweepResult(
            int pendingSwept,
            int runningSwept,
            LocalDateTime pendingCutoff,
            LocalDateTime runningCutoff
    ) {
        public int totalSwept() {
            return pendingSwept + runningSwept;
        }
    }
}
