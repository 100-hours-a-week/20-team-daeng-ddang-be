package com.daengddang.daengdong_map.repository;

public record RankingRetentionSummary(
        int dogGlobalDeleted,
        int dogRegionDeleted,
        int regionDeleted,
        int regionContributionDeleted
) {
}
