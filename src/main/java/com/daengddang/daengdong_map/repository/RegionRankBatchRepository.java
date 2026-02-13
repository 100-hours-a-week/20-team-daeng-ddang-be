package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;

public interface RegionRankBatchRepository {

    int upsertRanks(RankingPeriodType periodType, String periodValue);

    int deleteObsoleteRanks(RankingPeriodType periodType, String periodValue);
}
