package com.daengddang.daengdong_map.service.ranking.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ranking.batch.enabled", havingValue = "true")
public class RankingBatchScheduler {

    private final RankingBatchService rankingBatchService;

    @Scheduled(cron = "${ranking.batch.cron:0 0 0 * * *}", zone = "${ranking.batch.zone:Asia/Seoul}")
    public void runUpsert() {
        log.debug("Trigger ranking upsert batch scheduler");
        rankingBatchService.runUpsertAll();
    }

    @Scheduled(cron = "${ranking.batch.cleanup-cron:0 30 3 * * *}", zone = "${ranking.batch.zone:Asia/Seoul}")
    public void runCleanup() {
        log.debug("Trigger ranking cleanup batch scheduler");
        rankingBatchService.runCleanupAll();
    }
}
