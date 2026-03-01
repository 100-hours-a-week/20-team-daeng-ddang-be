package com.daengddang.daengdong_map.service.ranking.batch;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.repository.RankingCleanupBatchRepository;
import com.daengddang.daengdong_map.repository.RankingCleanupSummary;
import com.daengddang.daengdong_map.repository.RankingRetentionBatchRepository;
import com.daengddang.daengdong_map.repository.RankingRetentionSummary;
import com.daengddang.daengdong_map.repository.RankingUpsertBatchRepository;
import com.daengddang.daengdong_map.repository.RankingUpsertSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RankingBatchService {

    private final RankingPeriodResolver rankingPeriodResolver;
    private final RankingUpsertBatchRepository rankingUpsertBatchRepository;
    private final RankingCleanupBatchRepository rankingCleanupBatchRepository;
    private final RankingRetentionBatchRepository rankingRetentionBatchRepository;

    @Value("${ranking.batch.retention.week-keep-count:12}")
    private int weekKeepCount;

    @Value("${ranking.batch.retention.month-keep-count:12}")
    private int monthKeepCount;

    @Value("${ranking.batch.retention.year-keep-count:3}")
    private int yearKeepCount;

    public void runUpsertAll() {
        log.info("Ranking upsert batch started");

        for (RankingPeriodType periodType : RankingPeriodType.values()) {
            runUpsertByPeriodType(periodType);
        }

        log.info("Ranking upsert batch finished");
    }

    public void runCleanupAll() {
        log.info("Ranking cleanup batch started");

        for (RankingPeriodType periodType : RankingPeriodType.values()) {
            runCleanupByPeriodType(periodType);
        }

        log.info("Ranking cleanup batch finished");
    }

    public void runRetentionPurgeAll() {
        log.info(
                "Ranking retention purge batch started. weekKeepCount={}, monthKeepCount={}, yearKeepCount={}",
                weekKeepCount,
                monthKeepCount,
                yearKeepCount
        );

        for (RankingPeriodType periodType : RankingPeriodType.values()) {
            runRetentionPurgeByPeriodType(periodType);
        }

        log.info("Ranking retention purge batch finished");
    }

    private void runUpsertByPeriodType(RankingPeriodType periodType) {
        PeriodRange periodRange = rankingPeriodResolver.resolveCurrentPeriodRange(periodType);
        String periodValue = rankingPeriodResolver.resolvePeriodValue(periodType, periodRange);
        log.info(
                "Ranking upsert period start. periodType={}, periodValue={}, startAt={}, endAt={}",
                periodType,
                periodValue,
                periodRange.startAt(),
                periodRange.endAt()
        );

        runIntegratedRankingUpsert(periodType, periodValue, periodRange);

        log.info("Ranking upsert period finished. periodType={}, periodValue={}", periodType, periodValue);
    }

    private void runCleanupByPeriodType(RankingPeriodType periodType) {
        PeriodRange periodRange = rankingPeriodResolver.resolveCurrentPeriodRange(periodType);
        String periodValue = rankingPeriodResolver.resolvePeriodValue(periodType, periodRange);
        log.info(
                "Ranking cleanup period start. periodType={}, periodValue={}, startAt={}, endAt={}",
                periodType,
                periodValue,
                periodRange.startAt(),
                periodRange.endAt()
        );

        runIntegratedRankingCleanup(periodType, periodValue, periodRange);

        log.info("Ranking cleanup period finished. periodType={}, periodValue={}", periodType, periodValue);
    }

    private void runIntegratedRankingUpsert(RankingPeriodType periodType, String periodValue, PeriodRange periodRange) {
        RankingUpsertSummary summary = rankingUpsertBatchRepository.upsertAllRanks(
                periodType,
                periodValue,
                periodRange.startAt(),
                periodRange.endAt()
        );
        log.info(
                "Integrated ranking upsert completed. periodType={}, periodValue={}, dogGlobalUpserted={}, dogRegionUpserted={}, regionUpserted={}, regionContributionUpserted={}",
                periodType,
                periodValue,
                summary.dogGlobalUpserted(),
                summary.dogRegionUpserted(),
                summary.regionUpserted(),
                summary.regionContributionUpserted()
        );
    }

    private void runIntegratedRankingCleanup(RankingPeriodType periodType, String periodValue, PeriodRange periodRange) {
        RankingCleanupSummary summary = rankingCleanupBatchRepository.deleteObsoleteRanks(
                periodType,
                periodValue,
                periodRange.startAt(),
                periodRange.endAt()
        );
        log.info(
                "Integrated ranking cleanup completed. periodType={}, periodValue={}, dogGlobalDeleted={}, dogRegionDeleted={}, regionDeleted={}, regionContributionDeleted={}",
                periodType,
                periodValue,
                summary.dogGlobalDeleted(),
                summary.dogRegionDeleted(),
                summary.regionDeleted(),
                summary.regionContributionDeleted()
        );
    }

    private void runRetentionPurgeByPeriodType(RankingPeriodType periodType) {
        int keepCount = resolveKeepCount(periodType);
        String cutoffPeriodValue = rankingPeriodResolver.resolveRetentionCutoffPeriodValue(periodType, keepCount);
        RankingRetentionSummary summary = rankingRetentionBatchRepository.deleteOlderThan(periodType, cutoffPeriodValue);
        log.info(
                "Ranking retention purge completed. periodType={}, cutoffPeriodValue={}, dogGlobalDeleted={}, dogRegionDeleted={}, regionDeleted={}, regionContributionDeleted={}",
                periodType,
                cutoffPeriodValue,
                summary.dogGlobalDeleted(),
                summary.dogRegionDeleted(),
                summary.regionDeleted(),
                summary.regionContributionDeleted()
        );
    }

    private int resolveKeepCount(RankingPeriodType periodType) {
        return switch (periodType) {
            case WEEK -> weekKeepCount;
            case MONTH -> monthKeepCount;
            case YEAR -> yearKeepCount;
        };
    }
}
