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

    public void runAll() {
        log.info("Ranking batch started");

        for (RankingPeriodType periodType : RankingPeriodType.values()) {
            runByPeriodType(periodType);
        }

        log.info("Ranking batch finished");
    }

    private void runByPeriodType(RankingPeriodType periodType) {
        String periodValue = rankingPeriodResolver.resolveCurrentPeriodValue(periodType);
        log.info("Ranking batch period start. periodType={}, periodValue={}", periodType, periodValue);

        runDogGlobalRanking(periodType, periodValue);
        runDogRegionRanking(periodType, periodValue);
        runRegionRanking(periodType, periodValue);
        runRegionContributionRanking(periodType, periodValue);

        log.info("Ranking batch period finished. periodType={}, periodValue={}", periodType, periodValue);
    }

    private void runDogGlobalRanking(RankingPeriodType periodType, String periodValue) {
        int upserted = dogGlobalRankBatchRepository.upsertRanks(periodType, periodValue);
        int deleted = dogGlobalRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Dog global ranking batch completed. periodType={}, periodValue={}, upserted={}, deleted={}",
                periodType,
                periodValue,
                upserted,
                deleted
        );
    }

    private void runDogRegionRanking(RankingPeriodType periodType, String periodValue) {
        int upserted = dogRankBatchRepository.upsertRanks(periodType, periodValue);
        int deleted = dogRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Dog region ranking batch completed. periodType={}, periodValue={}, upserted={}, deleted={}",
                periodType,
                periodValue,
                upserted,
                deleted
        );
    }

    private void runRegionRanking(RankingPeriodType periodType, String periodValue) {
        int upserted = regionRankBatchRepository.upsertRanks(periodType, periodValue);
        int deleted = regionRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Region ranking batch completed. periodType={}, periodValue={}, upserted={}, deleted={}",
                periodType,
                periodValue,
                upserted,
                deleted
        );
    }

    private void runRegionContributionRanking(RankingPeriodType periodType, String periodValue) {
        int upserted = regionDogRankBatchRepository.upsertRanks(periodType, periodValue);
        int deleted = regionDogRankBatchRepository.deleteObsoleteRanks(periodType, periodValue);
        log.info(
                "Region contribution ranking batch completed. periodType={}, periodValue={}, upserted={}, deleted={}",
                periodType,
                periodValue,
                upserted,
                deleted
        );
    }
}
