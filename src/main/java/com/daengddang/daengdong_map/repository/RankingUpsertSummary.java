package com.daengddang.daengdong_map.repository;

public record RankingUpsertSummary(
        int dogGlobalUpserted,
        int dogRegionUpserted,
        int regionUpserted,
        int regionContributionUpserted
) {
}
