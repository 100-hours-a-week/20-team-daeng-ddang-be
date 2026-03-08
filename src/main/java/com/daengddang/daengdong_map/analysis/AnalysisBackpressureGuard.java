package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.common.exception.AnalysisBackpressureException;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisBackpressureGuard {

    private final AnalysisBackpressureProperties properties;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;

    public void validateOrThrow() {
        if (!properties.isEnabled()) {
            return;
        }

        long activeTasks = externalAnalysisTaskRepository.countActiveTasks();
        if (activeTasks >= properties.getMaxActiveTasks()) {
            throw new AnalysisBackpressureException(
                    properties.getRetryAfterSeconds(),
                    activeTasks,
                    properties.getMaxActiveTasks()
            );
        }
    }
}
