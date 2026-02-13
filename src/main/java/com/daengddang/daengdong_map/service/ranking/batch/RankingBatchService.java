package com.daengddang.daengdong_map.service.ranking.batch;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.repository.DogGlobalRankBatchRepository;
import com.daengddang.daengdong_map.repository.DogRankBatchRepository;
import com.daengddang.daengdong_map.repository.RegionDogRankBatchRepository;
import com.daengddang.daengdong_map.repository.RegionRankBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RankingBatchService {

    private final RankingPeriodResolver rankingPeriodResolver;
    private final DogGlobalRankBatchRepository dogGlobalRankBatchRepository;
    private final DogRankBatchRepository dogRankBatchRepository;
    private final RegionRankBatchRepository regionRankBatchRepository;
    private final RegionDogRankBatchRepository regionDogRankBatchRepository;

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

    private void runUpsertByPeriodType(RankingPeriodType periodType) {
        String periodValue = rankingPeriodResolver.resolveCurrentPeriodValue(periodType);
        log.info("Ranking upsert period start. periodType={}, periodValue={}", periodType, periodValue);

        runDogGlobalRankingUpsert(periodType, periodValue);
        runDogRegionRankingUpsert(periodType, periodValue);
        runRegionRankingUpsert(periodType, periodValue);
        runRegionContributionRankingUpsert(periodType, periodValue);

        log.info("Ranking upsert period finished. periodType={}, periodValue={}", periodType, periodValue);
    }

    private void runCleanupByPeriodType(RankingPeriodType periodType) {
        String periodValue = rankingPeriodResolver.resolveCurrentPeriodValue(periodType);
        log.info("Ranking cleanup period start. periodType={}, periodValue={}", periodType, periodValue);

        runDogGlobalRankingCleanup(periodType, periodValue);
        runDogRegionRankingCleanup(periodType, periodValue);
        runRegionRankingCleanup(periodType, periodValue);
        runRegionContributionRankingCleanup(periodType, periodValue);

        log.info("Ranking cleanup period finished. periodType={}, periodValue={}", periodType, periodValue);
    }

    private void runDogGlobalRankingUpsert(RankingPeriodType periodType, String periodValue) {
        int upserted = dogGlobalRankBatchRepository.upsertRanks(periodType, periodValue);
        log.info(
                "Dog global ranking upsert completed. periodType={}, periodValue={}, upserted={}",
                periodType,
                periodValue,
                upserted
        );
    }

    private void runDogGlobalRankingCleanup(RankingPeriodType periodType, String periodValue) {
        int deleted = dogGlobalRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Dog global ranking cleanup completed. periodType={}, periodValue={}, deleted={}",
                periodType,
                periodValue,
                deleted
        );
    }

    private void runDogRegionRankingUpsert(RankingPeriodType periodType, String periodValue) {
        int upserted = dogRankBatchRepository.upsertRanks(periodType, periodValue);
        log.info(
                "Dog region ranking upsert completed. periodType={}, periodValue={}, upserted={}",
                periodType,
                periodValue,
                upserted
        );
    }

    private void runDogRegionRankingCleanup(RankingPeriodType periodType, String periodValue) {
        int deleted = dogRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Dog region ranking cleanup completed. periodType={}, periodValue={}, deleted={}",
                periodType,
                periodValue,
                deleted
        );
    }

    private void runRegionRankingUpsert(RankingPeriodType periodType, String periodValue) {
        int upserted = regionRankBatchRepository.upsertRanks(periodType, periodValue);
        log.info(
                "Region ranking upsert completed. periodType={}, periodValue={}, upserted={}",
                periodType,
                periodValue,
                upserted
        );
    }

    private void runRegionRankingCleanup(RankingPeriodType periodType, String periodValue) {
        int deleted = regionRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Region ranking cleanup completed. periodType={}, periodValue={}, deleted={}",
                periodType,
                periodValue,
                deleted
        );
    }

    private void runRegionContributionRankingUpsert(RankingPeriodType periodType, String periodValue) {
        int upserted = regionDogRankBatchRepository.upsertRanks(periodType, periodValue);
        log.info(
                "Region contribution ranking upsert completed. periodType={}, periodValue={}, upserted={}",
                periodType,
                periodValue,
                upserted
        );
    }

    private void runRegionContributionRankingCleanup(RankingPeriodType periodType, String periodValue) {
        int deleted = regionDogRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Region contribution ranking cleanup completed. periodType={}, periodValue={}, deleted={}",
                periodType,
                periodValue,
                deleted
        );
    }
}
