package com.daengddang.daengdong_map.repository;

public record RankingCleanupSummary(
        int dogGlobalDeleted,
        int dogRegionDeleted,
        int regionDeleted,
        int regionContributionDeleted
) {
}
