package com.daengddang.daengdong_map.service.ranking.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ranking.batch.enabled", havingValue = "true")
public class RankingBatchScheduler {
    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final RankingBatchService rankingBatchService;
    private final RedissonClient redissonClient;

    @Value("${ranking.batch.lock.wait-ms:0}")
    private long lockWaitMs;

    @Value("${ranking.batch.lock.lease-ms:600000}")
    private long lockLeaseMs;

    @Value("${ranking.batch.lock.slot-key-prefix:lock:ranking:batch:slot}")
    private String slotKeyPrefix;

    @Value("${ranking.batch.lock.slot-ttl-ms:120000}")
    private long slotTtlMs;

    @Value("${ranking.batch.zone:Asia/Seoul}")
    private String batchZone;

    @Scheduled(cron = "${ranking.batch.cron:0 0 0 * * *}", zone = "${ranking.batch.zone:Asia/Seoul}")
    public void runUpsert() {
        executeWithDistributedLock(
                "lock:ranking:batch:upsert",
                "upsert",
                rankingBatchService::runUpsertAll
        );
    }

    @Scheduled(cron = "${ranking.batch.cleanup-cron:0 30 3 * * *}", zone = "${ranking.batch.zone:Asia/Seoul}")
    public void runCleanup() {
        executeWithDistributedLock(
                "lock:ranking:batch:cleanup",
                "cleanup",
                rankingBatchService::runCleanupAll
        );
    }

    @Scheduled(cron = "${ranking.batch.retention-cron:0 0 4 * * *}", zone = "${ranking.batch.zone:Asia/Seoul}")
    public void runRetentionPurge() {
        executeWithDistributedLock(
                "lock:ranking:batch:retention",
                "retention",
                rankingBatchService::runRetentionPurgeAll
        );
    }

    private void executeWithDistributedLock(String lockKey, String jobName, Runnable job) {
        String slotKey = buildSlotKey(jobName);
        if (!acquireExecutionSlot(slotKey, jobName)) {
            return;
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(lockWaitMs, lockLeaseMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.info(
                        "랭킹 배치 락 획득 실패로 실행 스킵 (Skip ranking batch due to lock contention): job={}, lockKey={}, waitMs={}, leaseMs={}",
                        jobName,
                        lockKey,
                        lockWaitMs,
                        lockLeaseMs
                );
                return;
            }

            log.debug("랭킹 배치 락 획득 성공 (Ranking batch lock acquired): job={}, lockKey={}", jobName, lockKey);
            job.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("랭킹 배치 락 대기 중 인터럽트 발생 (Interrupted while waiting for ranking batch lock): job={}, lockKey={}", jobName, lockKey, e);
        } catch (Exception e) {
            log.error("랭킹 배치 실행 중 오류 발생 (Ranking batch execution failed): job={}, lockKey={}", jobName, lockKey, e);
            throw e;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception unlockEx) {
                    log.warn("랭킹 배치 락 해제 실패 (Failed to unlock ranking batch lock): job={}, lockKey={}", jobName, lockKey, unlockEx);
                }
            }
        }
    }

    private String buildSlotKey(String jobName) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(batchZone));
        String slot = now.format(SLOT_FORMATTER);
        return slotKeyPrefix + ":" + jobName + ":" + slot;
    }

    private boolean acquireExecutionSlot(String slotKey, String jobName) {
        try {
            RBucket<String> slotBucket = redissonClient.getBucket(slotKey);
            boolean acquired = slotBucket.trySet("1", slotTtlMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.info(
                        "랭킹 배치 슬롯 점유로 실행 스킵 (Skip ranking batch due to execution slot occupied): job={}, slotKey={}, slotTtlMs={}",
                        jobName,
                        slotKey,
                        slotTtlMs
                );
            }
            return acquired;
        } catch (Exception e) {
            log.warn(
                    "랭킹 배치 슬롯 점유 실패로 실행 스킵 (Skip ranking batch due to execution slot acquisition failure): job={}, slotKey={}",
                    jobName,
                    slotKey,
                    e
            );
            return false;
        }
    }
}
