package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;

public interface RankingRetentionBatchRepository {

    RankingRetentionSummary deleteOlderThan(RankingPeriodType periodType, String cutoffPeriodValue);
}
