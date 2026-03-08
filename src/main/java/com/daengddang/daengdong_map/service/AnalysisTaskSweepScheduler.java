package com.daengddang.daengdong_map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "analysis.sweep.enabled", havingValue = "true")
public class AnalysisTaskSweepScheduler {

    private final AnalysisTaskSweepService analysisTaskSweepService;

    @Scheduled(fixedDelayString = "${analysis.sweep.fixed-delay-ms:30000}")
    public void runSweep() {
        AnalysisTaskSweepService.SweepResult result = analysisTaskSweepService.sweep();
        if (result.totalSwept() > 0) {
            log.info(
                    "분석 작업 sweep 실행 완료. total={}, pending={}, running={}, pendingCutoff={}, runningCutoff={}",
                    result.totalSwept(),
                    result.pendingSwept(),
                    result.runningSwept(),
                    result.pendingCutoff(),
                    result.runningCutoff()
            );
            return;
        }
        log.debug(
                "분석 작업 sweep 실행 완료. 정리 대상 없음. pendingCutoff={}, runningCutoff={}",
                result.pendingCutoff(),
                result.runningCutoff()
        );
    }
}
