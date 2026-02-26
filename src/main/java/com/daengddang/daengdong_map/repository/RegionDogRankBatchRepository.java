package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import java.time.LocalDateTime;

public interface RegionDogRankBatchRepository {

    int upsertRanks(RankingPeriodType periodType, String periodValue, LocalDateTime startAt, LocalDateTime endAt);

    int deleteObsoleteRanks(RankingPeriodType periodType, String periodValue, LocalDateTime startAt, LocalDateTime endAt);
}
